/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar Window;

@header {
//CHECKSTYLE:OFF
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
}
COMMA : ',';
COLON : ':';

WINDOW : 'window' | 'windows';

INCLUDE : 'include' | 'INCLUDE' | 'includes' | 'INCLUDES' | 'including' | 'INCLUDING';
EXCLUDE : 'exclude' | 'EXCLUDE' | 'excludes' | 'EXCLUDES' | 'excluding' | 'EXCLUDING';

NOW : 'NOW' | 'now';

FROM : 'FROM' | 'from';
EVERY : 'EVERY' | 'every';
TO : 'TO' | 'to' | 'until' | 'UNTIL';
AGO : 'AGO' | 'ago';

NUMBER : FIRST_DIGIT DIGIT*;
DAY_SPECIFIER : MONDAY | TUESDAY | WEDNESDAY | THURSDAY
                       | FRIDAY | SATURDAY | SUNDAY
                       | CURRENT_DAY_OF_WEEK
                       | WEEKEND | WEEKDAY | HOLIDAYS
                       ;

TIME_UNIT : SECOND_UNIT | MINUTE_UNIT | HOUR_UNIT | DAY_UNIT ;
IDENTIFIER : [a-zA-Z_][a-zA-Z_\.0-9]*;

WS : [ \r\t\u000C\n]+ -> skip;

fragment SECOND_UNIT : 'SECOND' | 'second' | 'seconds' | 'SECONDS';
fragment MINUTE_UNIT : 'MINUTE' | 'minute' | 'minutes' | 'MINUTES';
fragment HOUR_UNIT : 'HOUR' | 'hour' | 'hours' | 'HOURS';
fragment DAY_UNIT : 'DAY' | 'day' | 'days' | 'DAYS';
fragment MONDAY : 'MONDAY' | 'monday' | 'MONDAYS' | 'mondays';
fragment TUESDAY : 'TUESDAY' | 'tuesday' | 'TUESDAYS' | 'tuesdays';
fragment WEDNESDAY : 'WEDNESDAY' | 'wednesday' | 'WEDNESDAYS' | 'wednesdays';
fragment THURSDAY : 'THURSDAY' | 'thursday' | 'THURSDAYS' | 'thursdays';
fragment FRIDAY : 'FRIDAY' | 'friday' | 'FRIDAYS' | 'fridays';
fragment SATURDAY: 'SATURDAY' | 'saturday' | 'SATURDAYS' | 'saturdays';
fragment SUNDAY : 'SUNDAY' | 'sunday' | 'SUNDAYS' | 'sundays';
fragment CURRENT_DAY_OF_WEEK: 'this day of week' | 'THIS DAY OF WEEK' | 'this day of the week' | 'THIS DAY OF THE WEEK';
fragment WEEKEND : 'weekend' | 'WEEKEND' | 'weekends' | 'WEEKENDS';
fragment WEEKDAY: 'weekday' | 'WEEKDAY' | 'weekdays' | 'WEEKDAYS';
fragment HOLIDAYS: 'holiday' | 'HOLIDAY' | 'holidays' | 'HOLIDAYS';

fragment DIGIT : '0'..'9';
fragment FIRST_DIGIT : '1'..'9';

window : window_expression EOF;

window_expression : bin_width including_specifier? excluding_specifier? #NonRepeatingWindow
                  | bin_width skip_distance duration including_specifier? excluding_specifier? #RepeatingWindow
                  ;

excluding_specifier : EXCLUDE specifier_list
                    ;
including_specifier : INCLUDE specifier_list
                    ;

specifier : day_specifier (COLON specifier_arg_list)?
          ;

specifier_arg_list : identifier
                   | identifier COLON specifier_arg_list
                    ;

day_specifier : DAY_SPECIFIER ;

identifier : IDENTIFIER
          ;

specifier_list : specifier
               | specifier_list COMMA specifier
               ;

duration : FROM time_interval AGO? TO time_interval AGO? #FromToDuration
         | FROM time_interval AGO? #FromDuration
         ;

skip_distance : EVERY time_interval #SkipDistance
              ;

bin_width : time_interval WINDOW? #BinWidth
          ;

time_interval : time_amount time_unit #TimeInterval
              ;

time_amount : NUMBER #TimeAmount
            ;

time_unit : TIME_UNIT #TimeUnit
            ;
