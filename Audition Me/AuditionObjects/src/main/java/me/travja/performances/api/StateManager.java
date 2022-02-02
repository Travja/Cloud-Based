package me.travja.performances.api;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDeleteExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import me.travja.performances.api.models.*;
import me.travja.performances.serializers.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
public class StateManager {

    private static final ObjectMapper      mapper       = Util.getMapper();
    private static final DateTimeFormatter format       = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    @Setter(AccessLevel.PRIVATE)
    private static       StateManager      instance;
    private final        Regions           REGION       = Regions.US_WEST_2;
    private              AmazonDynamoDB    dynamoClient = AmazonDynamoDBClientBuilder.standard().build();
    private              DynamoDBMapper    dynamo       = new DynamoDBMapper(dynamoClient);

    private List<Person>           personCache      = new ArrayList<>();
    private Map<UUID, Performance> performanceCache = new HashMap<>();

    public static StateManager getInstance() {
        if (instance == null) {
            instance = new StateManager();
            instance.setup();
        }
        return instance;
    }

    public void cache(Person person) {
        if (person == null || personCache.stream().anyMatch(p -> p.getId().equals(person.getId())))
            return;

        personCache.add(person);
    }

    public void cache(Performance perf) {
        if (perf == null || performanceCache.keySet().stream().anyMatch(p -> p.equals(perf.getId())))
            return;

        performanceCache.put(perf.getId(), perf);
    }

    public void clearCache() {
        personCache.clear();
        performanceCache.clear();
    }

    private void initData() {
        System.out.println("Initializing Data...");
        Performer travis = getPerformerByName("Travis Eggett");
        if (travis == null) {
            save(new Performer("Travis Eggett", "teggett@student.neumont.edu", "phone number", "test"));
            save(new Performer("Chris Cantera", "ccantera@neumont.edu", "phone number", "test2")); //Hey look, we're performers
            save(new Director("Steven Spielberg", "spiel@berg.com", "123-456-7890", "test3"));
            save(new CastingDirector("Casting Director", "casting@director.com", "123-456-7891", "test3"));
            Performance swanLake = new Performance(
                    "Swan Lake",
                    "111 East St.",
                    getByEmail("spiel@berg.com", Director.class).get(),
                    getByEmail("casting@director.com", CastingDirector.class).get(),
                    new ArrayList<>(Arrays.asList(ZonedDateTime.now().plusDays(10))),
                    new ArrayList<>()
            );
            swanLake.setAuditionList(new ArrayList(
                    Arrays.asList(
                            new Audition(instance.getPerformerByName("Travis Eggett"),
                                    ZonedDateTime.now().plusDays(-5)
                            )
                    )
            ));
            save(swanLake);
            save(new Performance("Nutcracker", "Why are we doing ballets?",
                    getByEmail("spiel@berg.com", Director.class).get(),
                    getByEmail("casting@director.com", CastingDirector.class).get(),
                    Arrays.asList(ZonedDateTime.now().plusDays(12)),
                    new ArrayList<>()));
        } else {
            //Load data
        }

    }

    private void setup() {
        initDynamo();

        ObjectMapper mapper     = new ObjectMapper();
        SimpleModule dateModule = new SimpleModule("ZoneDateModule", new Version(1, 0, 0, "", null, null));
        dateModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        mapper.registerModule(dateModule);

        initData();
    }

    private void initDynamo() {
        List<String> names              = dynamoClient.listTables().getTableNames();
        boolean      personTableCreated = names.contains("Person");
        dynamo.newTableMapper(Person.class).createTableIfNotExists(new ProvisionedThroughput(25L, 25L));
        dynamo.newTableMapper(Performance.class).createTableIfNotExists(new ProvisionedThroughput(25L, 25L));
        dynamo.newTableMapper(Audition.class).createTableIfNotExists(new ProvisionedThroughput(25L, 25L));
        if (personTableCreated)
            System.out.println("Tables exist.");
        else
            System.out.println("Tables created.");
    }

    public <T extends Person> T save(T person) throws ConditionalCheckFailedException {
        dynamo.save(person);
        System.out.println("Saved " + person.getName());

        return person;
    }

    public Performance save(Performance performance) throws ConditionalCheckFailedException {
        for (Audition audition : performance.getAuditionList()) {
            dynamo.save(audition);
        }
        dynamo.save(performance);

        return performance;
    }

    public Performance delete(Performance performance) {
        dynamo.delete(performance);
        return performance;
    }

    public void deleteAudition(UUID id) {
        dynamo.delete(Audition.class, new DynamoDBDeleteExpression()
                .withExpressionAttributeValues(Map.of("id", new AttributeValue(id.toString()))));
    }

    public boolean hasPerformance(Performance performance) {
        return getPerformanceById(performance.getId()).isPresent();
    }

    public Optional<Performance> getPerformanceById(UUID id) {
        if (performanceCache.containsKey(id))
            return Optional.ofNullable(performanceCache.get(id));
        Performance perf = dynamo.load(
                Performance.class,
                id,
                DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
        );

        if (perf != null)
            cache(perf);

        return Optional.ofNullable(perf);
    }

    public Optional<Performance> getPerformanceByName(String name) {
        Map<String, Condition> filter = new HashMap<>();
        filter.put("title", new Condition().withComparisonOperator(ComparisonOperator.CONTAINS)
                .withAttributeValueList(new AttributeValue().withS(name)));
        Optional<Performance> perf = dynamo.scan(
                Performance.class,
                new DynamoDBScanExpression().withScanFilter(filter),
                DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
        ).stream().findFirst();

        perf.ifPresent(p -> cache(perf.get()));

        return perf;
    }

    public Performer getPerformerByName(String name) {
        Optional<Person> person = personCache.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name) && p.getType().equals("Performer"))
                .findFirst();
        if (person.isPresent())
            return (Performer) person.get();

        Map<String, Condition> filter = new HashMap<>();
        filter.put("name", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(name)));
        Performer perf = dynamo.scan(
                Performer.class,
                new DynamoDBScanExpression().withScanFilter(filter),
                DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
        ).stream().findFirst().orElse(null);

        if (perf != null)
            cache(perf);

        return perf;
    }

    public Performer getPerformerById(UUID id) {
        Optional<Person> pers = personCache.stream()
                .filter(p -> p.getId().equals(id) && p.getType().equals("Performer"))
                .findFirst();
        if (pers.isPresent())
            return (Performer) pers.get();

        Map<String, Condition> filter = new HashMap<>();
        filter.put("id", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(id.toString())));
        Performer person = dynamo.scan(Performer.class,
                new DynamoDBScanExpression().withScanFilter(filter),
                DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
        ).stream().findFirst().orElse(null);

        if (person != null)
            cache(person);

        return person;

    }

    public <T extends Person> Optional<T> getByEmail(String email, Class<T> target) {
        Optional<Person> pers = personCache.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(email))
                .findFirst();
        if (pers.isPresent())
            return (Optional<T>) pers;


        Map<String, Condition> filter = new HashMap<>();
        filter.put("email", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(email)));
        Optional<T> person = dynamo.scan(
                target,
                new DynamoDBScanExpression().withScanFilter(filter),
                DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
        ).stream().findFirst();

        if (person.isPresent() && target.equals(Person.class)) {
            person = convertToRealPerson(person, filter);
        }
        person.ifPresent(p -> cache(p));

        return person;

    }

    public <T extends Person> Optional<T> getById(UUID id) {
        Optional<Person> pers = personCache.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        if (pers.isPresent())
            return (Optional<T>) pers;


        Map<String, Condition> filter = new HashMap<>();
        filter.put("id", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(id.toString())));
        Optional<T> person = (Optional<T>) dynamo.scan(
                Person.class,
                new DynamoDBScanExpression().withScanFilter(filter),
                DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
        ).stream().findFirst();

        person.ifPresent(p -> cache(p));

        return convertToRealPerson(person, filter);
    }

    public Optional<Audition> getAudition(UUID id) {
        Map<String, Condition> filter = new HashMap<>();
        filter.put("id", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(id.toString())));

        return dynamo.scan(
                Audition.class,
                new DynamoDBScanExpression()
                        .withScanFilter(filter)
                        .withConsistentRead(true)
        ).stream().findFirst();
    }

    public List<Performance> getPerformances() {
        List<Performance> list = new ArrayList<>();
        List<Performance> scan = dynamo.scan(Performance.class, new DynamoDBScanExpression().withConsistentRead(true));
        System.out.println("There are " + scan.size() + " performances in the db.");
        for (Performance performance : scan) {
            list.add(performance);
            cache(performance);
        }
        return list;
    }

    public List<Performer> getPerformers() {
        List<Performer> list = new ArrayList<>();
        List<Performer> scan = dynamo.scan(Performer.class, new DynamoDBScanExpression()
                .withScanFilter(Map.of("type", new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue("Performer"))))
                .withConsistentRead(true));
        System.out.println("There are " + scan.size() + " performers in the db.");
        for (Performer performer : scan) {
            list.add(performer);
            cache(performer);
        }
        return list;
    }

    public <T extends Person> Optional<T> convertToRealPerson(Optional<T> person, Map<String, Condition> filter) {
        if (person.isPresent()) {
            Person p         = person.get();
            Person transform = null;

            if (p.getType().equals("CastingDirector"))
                transform = new CastingDirector(p.getId(), p.getName(), p.getEmail(), p.getPhone(),
                        p.getPassword());
            else if (p.getType().equals("Director"))
                transform = new Director(p.getId(), p.getName(), p.getEmail(), p.getPhone(),
                        p.getPassword());
            else if (p.getType().equals("Performer"))
                person = (Optional<T>) dynamo.scan(
                        Performer.class,
                        new DynamoDBScanExpression().withScanFilter(filter),
                        DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
                ).stream().findFirst();

            if (transform != null)
                person = (Optional<T>) Optional.of(transform);
        }

        return person;
    }

}
