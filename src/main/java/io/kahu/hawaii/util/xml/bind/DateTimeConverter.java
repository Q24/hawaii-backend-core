/**
 * Copyright 2015 Q24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kahu.hawaii.util.xml.bind;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class DateTimeConverter {
    public static DateTime getDateTime(Calendar calendar) {
        // @formatter:off
            return new DateTime(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DATE),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND),
                    calendar.get(Calendar.MILLISECOND),
                    DateTimeZone.forTimeZone(calendar.getTimeZone()));
         // @formatter:on
    }

    public static LocalDate getLocalDate(Calendar calendar) {
        // @formatter:off
            return new LocalDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DATE));
         // @formatter:on
    }

    public static LocalTime getLocalTime(Calendar calendar) {
        // @formatter:off
            return new LocalTime(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND),
                    calendar.get(Calendar.MILLISECOND)
                    );
         // @formatter:on
    }

    public static DateTime parseDateTime(String input) {
        Calendar parsed = DatatypeConverter.parseDateTime(input);
        return getDateTime(parsed);
    }

    public static String printDateTime(DateTime input) {
        Calendar cal = input.toGregorianCalendar();
        return DatatypeConverter.printDateTime(cal);
    }

    public static LocalDate parseDate(String input) {
        Calendar parsed = DatatypeConverter.parseDate(input);
        return getLocalDate(parsed);
    }

    public static String printDate(LocalDate input) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(input.toDate());
        return DatatypeConverter.printDate(cal);
    }

    public static LocalTime parseTime(String input) {
        Calendar parsed = DatatypeConverter.parseTime(input);
        return getLocalTime(parsed);
    }

    public static String printTime(LocalTime input) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(input.toDateTimeToday().toDate());
        return DatatypeConverter.printTime(cal);
    }
}