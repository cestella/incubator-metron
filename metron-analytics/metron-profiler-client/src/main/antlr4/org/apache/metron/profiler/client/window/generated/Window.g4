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

BIN : 'bin' | 'BIN' | 'bins' | 'BINS';

INCLUDE : 'include' | 'INCLUDE' | 'includes' | 'INCLUDES';
EXCLUDE : 'exclude' | 'EXCLUDE' | 'excludes' | 'EXCLUDES';

NOW : 'NOW' | 'now';

FROM : 'FROM' | 'from';
EVERY : 'EVERY' | 'every';
TO : 'TO' | 'to' | 'until' | 'UNTIL';
AGO : 'AGO' | 'ago';

NUMBER : FIRST_DIGIT DIGIT*;
WS : [ \r\t\u000C\n]+ -> skip;
DAY_SPECIFIER : MONDAY | TUESDAY | WEDNESDAY | THURSDAY
                       | FRIDAY | SATURDAY | SUNDAY
                       | CURRENT_DAY_OF_WEEK
                       | WEEKEND | WEEKDAY
                       ;

TIME_UNIT : SECOND_UNIT | HOUR_UNIT | DAY_UNIT | MONTH_UNIT;

fragment SECOND_UNIT : 'SECOND' | 'second' | 'seconds' | 'SECONDS';
fragment HOUR_UNIT : 'HOUR' | 'hour' | 'hours' | 'HOURS';
fragment DAY_UNIT : 'DAY' | 'day' | 'days' | 'DAYS';
fragment MONTH_UNIT : 'MONTH' | 'month' | 'months' | 'MONTHS';
fragment MONDAY : 'MONDAY' | 'monday';
fragment TUESDAY : 'TUESDAY' | 'tuesday';
fragment WEDNESDAY : 'WEDNESDAY' | 'wednesday';
fragment THURSDAY : 'THURSDAY' | 'thursday';
fragment FRIDAY : 'FRIDAY' | 'friday';
fragment SATURDAY: 'SATURDAY' | 'saturday';
fragment SUNDAY : 'SUNDAY' | 'sunday';
fragment CURRENT_DAY_OF_WEEK: 'this day of week' | 'THIS DAY OF WEEK';
fragment WEEKEND : 'weekend' | 'WEEKEND' | 'weekends' | 'WEEKENDS';
fragment WEEKDAY: 'weekday' | 'WEEKDAY' | 'weekdays' | 'WEEKDAYS';

fragment DIGIT : '0'..'9';
fragment FIRST_DIGIT : '1'..'9';

window : window_expression EOF;

window_expression : bin_width including_specifier? excluding_specifier? #NonRepeatingWindow
                  | bin_width skip_distance duration including_specifier? excluding_specifier? #RepeatingWindow
                  ;

excluding_specifier : specifier_list #Excluding
                    ;
including_specifier : specifier_list #Including
                    ;

specifier : DAY_SPECIFIER #DaySpecifier
          ;

specifier_list : specifier
               | specifier_list COMMA specifier
               ;

duration : FROM time_interval TO time_interval AGO? #FromToDuration
         | TO time_interval AGO? #ToDuration
         ;

skip_distance : EVERY time_interval #SkipDistance
              ;

bin_width : time_interval BIN #BinWidth
          ;

time_interval : NUMBER TIME_UNIT #TimeInterval
              | NOW #TimeIntervalNow
              ;
