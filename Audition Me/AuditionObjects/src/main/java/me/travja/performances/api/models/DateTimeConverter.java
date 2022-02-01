package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeConverter implements DynamoDBTypeConverter<String, ZonedDateTime> {
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");

    @Override
    public String convert(ZonedDateTime zonedDateTime) {
        return format.format(zonedDateTime);
    }

    @Override
    public ZonedDateTime unconvert(String s) {
        return ZonedDateTime.from(format.parse(s));
    }
}
