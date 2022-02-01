package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ListDateTimeConverter implements DynamoDBTypeConverter<String, List<ZonedDateTime>> {
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");

    @Override
    public String convert(List<ZonedDateTime> zonedDateTime) {
        StringBuilder sb = new StringBuilder();
        zonedDateTime.forEach(zone -> {
            sb.append(format.format(zone))
                    .append(";");
        });
        return sb.toString();
    }

    @Override
    public List<ZonedDateTime> unconvert(String s) {
        List<ZonedDateTime> times = new ArrayList<>();
        for (String str : s.split(";")) {
            times.add(ZonedDateTime.from(format.parse(str)));
        }
        return times;
    }
}
