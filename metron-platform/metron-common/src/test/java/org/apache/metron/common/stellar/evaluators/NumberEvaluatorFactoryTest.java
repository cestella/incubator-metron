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

package org.apache.metron.common.stellar.evaluators;

import org.apache.metron.common.dsl.ParseException;
import org.apache.metron.common.stellar.generated.StellarParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NumberEvaluatorFactoryTest {
  NumberEvaluator<StellarParser.IntLiteralContext> intLiteralContextNumberEvaluator;
  NumberEvaluator<StellarParser.DoubleLiteralContext> doubleLiteralContextNumberEvaluator;
  NumberEvaluator<StellarParser.FloatLiteralContext> floatLiteralContextNumberEvaluator;
  NumberEvaluator<StellarParser.LongLiteralContext> longLiteralContextNumberEvaluator;
  NumberEvaluatorFactory numberEvaluatorFactory;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    intLiteralContextNumberEvaluator = mock(IntLiteralEvaluator.class);
    doubleLiteralContextNumberEvaluator = mock(DoubleLiteralEvaluator.class);
    floatLiteralContextNumberEvaluator = mock(FloatLiteralEvaluator.class);
    longLiteralContextNumberEvaluator = mock(LongLiteralEvaluator.class);
    numberEvaluatorFactory = new NumberEvaluatorFactory(intLiteralContextNumberEvaluator, doubleLiteralContextNumberEvaluator, floatLiteralContextNumberEvaluator, longLiteralContextNumberEvaluator);
  }

  @Test
  public void verifyIntLiteralContextIsProperlyEvaluated() throws Exception {
    StellarParser.IntLiteralContext context = mock(StellarParser.IntLiteralContext.class);
    numberEvaluatorFactory.evaluate(context);

    verify(intLiteralContextNumberEvaluator).evaluate(context);
    verifyZeroInteractions(doubleLiteralContextNumberEvaluator, floatLiteralContextNumberEvaluator, longLiteralContextNumberEvaluator);
  }

  @Test
  public void verifyDoubleLiteralContextIsProperlyEvaluated() throws Exception {
    StellarParser.DoubleLiteralContext context = mock(StellarParser.DoubleLiteralContext.class);
    numberEvaluatorFactory.evaluate(context);

    verify(doubleLiteralContextNumberEvaluator).evaluate(context);
    verifyZeroInteractions(intLiteralContextNumberEvaluator, floatLiteralContextNumberEvaluator, longLiteralContextNumberEvaluator);
  }

  @Test
  public void verifyFloatLiteralContextIsProperlyEvaluated() throws Exception {
    StellarParser.FloatLiteralContext context = mock(StellarParser.FloatLiteralContext.class);
    numberEvaluatorFactory.evaluate(context);

    verify(floatLiteralContextNumberEvaluator).evaluate(context);
    verifyZeroInteractions(doubleLiteralContextNumberEvaluator, intLiteralContextNumberEvaluator, longLiteralContextNumberEvaluator);
  }

  @Test
  public void verifyLongLiteralContextIsProperlyEvaluated() throws Exception {
    StellarParser.LongLiteralContext context = mock(StellarParser.LongLiteralContext.class);
    numberEvaluatorFactory.evaluate(context);

    verify(longLiteralContextNumberEvaluator).evaluate(context);
    verifyZeroInteractions(doubleLiteralContextNumberEvaluator, floatLiteralContextNumberEvaluator, intLiteralContextNumberEvaluator);
  }

  @Test
  public void verifyExceptionThrownForUnsupportedContextType() throws Exception {
    StellarParser.VariableContext context = mock(StellarParser.VariableContext.class);

    exception.expect(ParseException.class);
    exception.expectMessage("Does not support evaluation for type " + context.getClass());

    numberEvaluatorFactory.evaluate(context);

    verifyZeroInteractions(longLiteralContextNumberEvaluator, doubleLiteralContextNumberEvaluator, floatLiteralContextNumberEvaluator, intLiteralContextNumberEvaluator);
  }
}
