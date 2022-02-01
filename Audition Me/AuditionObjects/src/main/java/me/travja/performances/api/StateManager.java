package me.travja.performances.api;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class StateManager<T> {

    private final static String                PERFORMER_TABLE        = "Performer";
    private final static String                DIRECTOR_TABLE         = "Director";
    private final static String                CASTING_DIRECTOR_TABLE = "Director";
    private final static String                PERFORMANCE_TABLE      = "Performance";
    @Setter(AccessLevel.PRIVATE)
    private static       StateManager          instance;
    private static       ObjectMapper          mapper                 = Util.getMapper();
    private static       DateTimeFormatter     format                 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final        Regions               REGION                 = Regions.US_WEST_2;
    // This will be offloaded to Dynamo
    // TODO Load data from Dynamo
    private              List<Performer>       performers             = new ArrayList<>();
    private              List<Director>        directors              = new ArrayList<>();
    private              List<CastingDirector> castingDirectors       = new ArrayList<>();
    private              List<Performance>     performances           = new ArrayList<>();
    //    private        Table                 personTable;
    //    private        Table                 performanceTable;
    private              AmazonDynamoDB        dynamo                 = AmazonDynamoDBClientBuilder.standard().build();
    private              DynamoDBMapper        dbmapper               = new DynamoDBMapper(dynamo);

    public static StateManager getInstance() {
        if (instance == null) {
            instance = new StateManager();
            instance.setup();
        }
        return instance;
    }

    private void initData() {
        System.out.println("Initializing Data...");
        Performer travis = getOrLoadPerformerByName("Travis Eggett");
        if (travis == null) {
            save(new Performer("Travis Eggett", "teggett@student.neumont.edu", "phone number", "test"));
            save(new Performer("Chris Cantera", "ccantera@neumont.edu", "phone number", "test2")); //Hey look, we're performers
            save(new Director("Steven Spielberg", "spiel@berg.com", "123-456-7890", "test3"));
        } else {

        }
    }

    private void setup() {
        initDynamo();

        ObjectMapper mapper     = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, "", null, null));
        testModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        mapper.registerModule(testModule);

        initData();

        performances.add(
                new Performance(
                        "Swan Lake",
                        "111 East St.",
                        getOrLoadByEmail("spiel@berg.com", Director.class).get(),
                        save(new CastingDirector()),
                        new ArrayList<>(Arrays.asList(ZonedDateTime.now().plusDays(10))),
                        new ArrayList<>()
                )
        );
        performances.add(new Performance());

        Optional<Performance> zero = instance.getPerformanceById(0);
        zero.orElseThrow(() ->
                        new NoSuchElementException("Performance with ID '0' doesn't exist"))
                .setAuditionList(new ArrayList(
                        Arrays.asList(
                                new Audition(instance.getPerformerByName("Travis Eggett"),
                                        ZonedDateTime.now().plusDays(-5)
                                )
                        )
                ));
    }

    private void initDynamo() {
        List<String> names              = dynamo.listTables().getTableNames();
        boolean      personTableCreated = names.contains(PERFORMER_TABLE);
        dbmapper.newTableMapper(Person.class).createTableIfNotExists(new ProvisionedThroughput(25L, 25L));
        dbmapper.newTableMapper(Performance.class).createTableIfNotExists(new ProvisionedThroughput(25L, 25L));
        if (personTableCreated) {
            System.out.println("Performer table exists.");
            dbmapper.scan(Performer.class, new DynamoDBScanExpression()
                            .withExpressionAttributeValues(Map.of("type", new AttributeValue("Performer")))
                            .withConsistentRead(true)).stream()
                    .forEach(person -> {
                        performers.add(person);
                        System.out.println("Loaded performer" + person.getName());
                    });
            dbmapper.scan(CastingDirector.class, new DynamoDBScanExpression()
                            .withExpressionAttributeValues(Map.of("type", new AttributeValue("CastingDirector")))
                            .withConsistentRead(true)).stream()
                    .forEach(person -> {
                        castingDirectors.add(person);
                        System.out.println("Loaded casting director " + person.getName());
                    });
            dbmapper.scan(Director.class, new DynamoDBScanExpression()
                            .withExpressionAttributeValues(Map.of("type", new AttributeValue("Director")))
                            .withConsistentRead(true)).stream()
                    .forEach(person -> {
                        directors.add(person);
                        System.out.println("Loaded director " + person.getName());
                    });
        }
    }

    public <T extends Person> T save(T person) throws ConditionalCheckFailedException {
        if (person instanceof CastingDirector) {
            castingDirectors.add((CastingDirector) person);
        } else if (person instanceof Director) {
            directors.add((Director) person);
        } else if (person instanceof Performer) {
            performers.add((Performer) person);
        }

        dbmapper.save(person);
        System.out.println("Saved " + person.getName());

        return person;
    }

    public Performance save(Performance performance) throws ConditionalCheckFailedException, JsonProcessingException {
        if (!hasPerformance(performance))
            performances.add(performance);
        dbmapper.save(performance);

        return performance;
    }

    public boolean hasPerformance(Performance performance) {
        return getPerformanceById(performance.getId()).isPresent();
    }

    public Performance getOrLoadPerformance(long id) {
        Optional<Performance> cached = getPerformanceById(id);
        if (cached.isPresent())
            return cached.get();
        else {
            Performance perf = dbmapper.load(
                    Performance.class,
                    id,
                    DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
            );
            performances.add(perf);
            return perf;
        }
    }

    public Performer getOrLoadPerformerByName(String name) {
        Performer cached = getPerformerByName(name);
        if (cached != null)
            return cached;
        else {
            Map<String, Condition> filter = new HashMap<>();
            filter.put("name", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                    .withAttributeValueList(new AttributeValue().withS(name)));
            Performer perf = dbmapper.scan(
                    Performer.class,
                    new DynamoDBScanExpression().withScanFilter(filter),
                    DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
            ).stream().findFirst().orElse(null);
            if (perf != null)
                performers.add(perf);
            return perf;
        }
    }

    public <T extends Person> Optional<T> getOrLoadByEmail(String email, Class<T> target) {
        Optional<T> cached = getByEmail(email, target);
        if (cached != null && cached.isPresent())
            return cached;
        else {
            Map<String, Condition> filter = new HashMap<>();
            filter.put("email", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                    .withAttributeValueList(new AttributeValue().withS(email)));
            Optional<T> perf = dbmapper.scan(
                    target,
                    new DynamoDBScanExpression().withScanFilter(filter),
                    DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
            ).stream().findFirst();
//            if (perf != null)
//                performers.add(perf);
            return perf;
        }
    }

    public Performer getPerformerById(UUID id) {
        return performers.stream()
                .filter(performer -> performer.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Performer with ID '" + id + "' doesn't exist"));
    }

    public Performer getPerformerByName(String name) {
        Pattern regex = Pattern.compile(".*" + name.toLowerCase() + ".*");

        return performers.stream().filter(perf -> {
            Matcher matcher = regex.matcher(perf.getName().toLowerCase());
            return matcher.find();
        }).findFirst().orElse(null);
    }

    public Optional<Performance> getPerformanceById(long id) {
        return performances.stream()
                .filter(performance -> performance.getId() == id)
                .findFirst();
    }

    public Performance getPerformanceByName(String title) {
        Pattern regex = Pattern.compile(".*" + title.toLowerCase() + ".*");

        return performances.stream().filter(perf -> {
            Matcher matcher = regex.matcher(perf.getTitle().toLowerCase());
            return matcher.find();
        }).findFirst().orElse(null);
    }

    public void addPerformance(Performance performance) {
        try {
            save(performance);
        } catch (JsonProcessingException e) {
            System.err.println("Could not save performance");
            e.printStackTrace();
        }
    }

    public <T extends Person> Optional<T> getByEmail(String email, Class<T> target) {


        Map<String, Condition> filter = new HashMap<>();
        filter.put("email", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(email)));
        Optional<T> person = dbmapper.scan(
                target,
                new DynamoDBScanExpression().withScanFilter(filter),
                DynamoDBMapperConfig.builder().withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build()
        ).stream().findFirst();
        if (person != null) {
//            performers.add(person);

        }

        return person;

//        List<Person> allPeople = new ArrayList<>();
//        allPeople.addAll(getPerformers());
//        allPeople.addAll(getDirectors());
//        allPeople.addAll(getCastingDirectors());
//
//        return allPeople.stream()
//                .filter(person -> {
//                    System.out.println(person.getEmail());
//                    return person.getEmail().equalsIgnoreCase(email);
//                })
//                .findFirst();
    }

}
