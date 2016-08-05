/**
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

package org.apache.metron.common.transformation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import org.apache.metron.common.dsl.Token;
import org.apache.metron.common.dsl.TransformationFunctions;
import org.apache.metron.common.dsl.FunctionMarker;
import org.apache.metron.common.dsl.ParseException;
import org.apache.metron.common.dsl.VariableResolver;
import org.apache.metron.common.query.BooleanOp;
import org.apache.metron.common.transformation.generated.TransformationBaseListener;
import org.apache.metron.common.transformation.generated.TransformationParser;
import org.apache.metron.common.utils.ConversionUtils;

import java.util.*;
import java.util.function.Function;

public class TransformationCompiler extends TransformationBaseListener {
  private VariableResolver resolver = null;
  private Stack<Token> tokenStack = new Stack<>();
  public TransformationCompiler(VariableResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void enterTransformation(TransformationParser.TransformationContext ctx) {
    tokenStack.clear();
  }

  private boolean handleIn(Token<?> left, Token<?> right) {
    Object key = null;

    Set<Object> set = null;
    if(left.getValue() instanceof Collection) {
      set = new HashSet<>((List<Object>) left.getValue());
    }
    else if(left.getValue() != null) {
      set = ImmutableSet.of(left.getValue());
    }
    else {
      set = new HashSet<>();
    }


    key = right.getValue();
    if(key == null || set.isEmpty()) {
      return false;
    }
    return set.contains(key);
  }


  @Override
  public void exitArithExpr_plus(TransformationParser.ArithExpr_plusContext ctx) {
    Token<?> right = popStack();
    Token<?> left = popStack();
    Number r = (Number)right.getValue();
    Number l = (Number)left.getValue();
    tokenStack.push(new Token<>(l.doubleValue() + r.doubleValue(), Double.class));
  }


  @Override
  public void exitArithExpr_minus(TransformationParser.ArithExpr_minusContext ctx) {
    Token<?> right = popStack();
    Token<?> left = popStack();
    Number r = (Number)right.getValue();
    Number l = (Number)left.getValue();
    tokenStack.push(new Token<>(l.doubleValue() - r.doubleValue(), Double.class));
  }


  @Override
  public void exitArithExpr_div(TransformationParser.ArithExpr_divContext ctx) {
    Token<?> right = popStack();
    Token<?> left = popStack();
    Number r = (Number)right.getValue();
    Number l = (Number)left.getValue();
    tokenStack.push(new Token<>(l.doubleValue() / r.doubleValue(), Double.class));
  }


  @Override
  public void exitArithExpr_mul(TransformationParser.ArithExpr_mulContext ctx) {
    Token<?> right = popStack();
    Token<?> left = popStack();
    Number r = (Number)right.getValue();
    Number l = (Number)left.getValue();
    tokenStack.push(new Token<>(l.doubleValue() * r.doubleValue(), Double.class));
  }

  @Override
  public void exitTernaryFunc(TransformationParser.TernaryFuncContext ctx) {
    Token<?> elseExpr = popStack();
    Token<?> thenExpr = popStack();
    Token<?> ifExpr = popStack();
    boolean b = ((Token<Boolean>)ifExpr).getValue();
    if(b) {
      tokenStack.push(thenExpr);
    }
    else {
      tokenStack.push(elseExpr);
    }
  }

  @Override
  public void exitInExpression(TransformationParser.InExpressionContext ctx) {
    Token<?> left = popStack();
    Token<?> right = popStack();
    tokenStack.push(new Token<>(handleIn(left, right), Boolean.class));
  }


  @Override
  public void exitNInExpression(TransformationParser.NInExpressionContext ctx) {
    Token<?> left = popStack();
    Token<?> right = popStack();
    tokenStack.push(new Token<>(!handleIn(left, right), Boolean.class));
  }

  @Override
  public void exitNotFunc(TransformationParser.NotFuncContext ctx) {
    Token<Boolean> arg = (Token<Boolean>) popStack();
    tokenStack.push(new Token<>(!arg.getValue(), Boolean.class));
  }

  @Override
  public void exitVariable(TransformationParser.VariableContext ctx) {
    tokenStack.push(new Token<>(resolver.resolve(ctx.getText()), Object.class));
  }

  @Override
  public void exitStringLiteral(TransformationParser.StringLiteralContext ctx) {
    tokenStack.push(new Token<>(ctx.getText().substring(1, ctx.getText().length() - 1), String.class));
  }


  @Override
  public void exitIntLiteral(TransformationParser.IntLiteralContext ctx) {
    tokenStack.push(new Token<>(Integer.parseInt(ctx.getText()), Integer.class));
  }

  @Override
  public void exitDoubleLiteral(TransformationParser.DoubleLiteralContext ctx) {
    tokenStack.push(new Token<>(Double.parseDouble(ctx.getText()), Double.class));
  }

  @Override
  public void exitLogicalExpressionAnd(TransformationParser.LogicalExpressionAndContext ctx) {
    Token<?> left = popStack();
    Token<?> right = popStack();
    tokenStack.push(new Token<>(booleanOp(left, right, (l, r) -> l && r, "&&"), Boolean.class));
  }

  @Override
  public void exitLogicalExpressionOr(TransformationParser.LogicalExpressionOrContext ctx) {
    Token<?> left = popStack();
    Token<?> right = popStack();

    tokenStack.push(new Token<>(booleanOp(left, right, (l, r) -> l || r, "||"), Boolean.class));
  }

  @Override
  public void exitLogicalConst(TransformationParser.LogicalConstContext ctx) {
    Boolean b = null;
    switch(ctx.getText().toUpperCase()) {
      case "TRUE":
        b = true;
        break;
      case "FALSE":
        b = false;
        break;
      default:
        throw new ParseException("Unable to process " + ctx.getText() + " as a boolean constant");
    }
    tokenStack.push(new Token<>(b, Boolean.class));
  }

  private boolean booleanOp(Token<?> left, Token<?> right, BooleanOp op, String opName)
  {
    Boolean l = ConversionUtils.convert(left.getValue(), Boolean.class);
    Boolean r = ConversionUtils.convert(right.getValue(), Boolean.class);
    if(l == null || r == null) {
      throw new ParseException("Unable to operate on " + left.getValue()  + " " + opName + " " + right.getValue() + ", null value");
    }
    return op.op(l, r);

  }

  @Override
  public void exitTransformationFunc(TransformationParser.TransformationFuncContext ctx) {
    String funcName = ctx.getChild(0).getText();
    Function<List<Object>, Object> func;
    try {
      func = TransformationFunctions.valueOf(funcName);
    }
    catch(IllegalArgumentException iae) {
      throw new ParseException("Unable to find string function " + funcName + ".  Valid functions are "
              + Joiner.on(',').join(TransformationFunctions.values())
      );
    }
    Token<?> left = popStack();
    List<Object> argList = null;
    if(left.getUnderlyingType().equals(List.class)) {
      argList = (List<Object>) left.getValue();
    }
    else {
      throw new ParseException("Unable to process in clause because " + left.getValue() + " is not a set");
    }
    Object result = func.apply(argList);
    tokenStack.push(new Token<>(result, Object.class));
  }


  @Override
  public void exitExistsFunc(TransformationParser.ExistsFuncContext ctx) {
    String variable = ctx.getChild(2).getText();
    boolean exists = resolver.resolve(variable) != null;
    tokenStack.push(new Token<>(exists, Boolean.class));
  }

  @Override
  public void enterFunc_args(TransformationParser.Func_argsContext ctx) {
    tokenStack.push(new Token<>(new FunctionMarker(), FunctionMarker.class));
  }


  @Override
  public void exitFunc_args(TransformationParser.Func_argsContext ctx) {
    LinkedList<Object> args = new LinkedList<>();
    while(true) {
      Token<?> token = popStack();
      if(token.getUnderlyingType().equals(FunctionMarker.class)) {
        break;
      }
      else {
        args.addFirst(token.getValue());
      }
    }
    tokenStack.push(new Token<>(args, List.class));
  }


  @Override
  public void exitList_entity(TransformationParser.List_entityContext ctx) {
    LinkedList<Object> args = new LinkedList<>();
    while(true) {
      Token<?> token = popStack();
      if(token.getUnderlyingType().equals(FunctionMarker.class)) {
        break;
      }
      else {
        args.addFirst(token.getValue());
      }
    }
    tokenStack.push(new Token<>(args, List.class));
  }
  private <T extends Comparable<T>> boolean compare(T l, T r, String op) {
    if(op.equals("==")) {
        return l.compareTo(r) == 0;
      }
      else if(op.equals("!=")) {
        return l.compareTo(r) != 0;
      }
      else if(op.equals("<")) {
        return l.compareTo(r) < 0;
      }
      else if(op.equals(">")) {
        return l.compareTo(r) > 0;
      }
      else if(op.equals(">=")) {
        return l.compareTo(r) >= 0;
      }
      else {
        return l.compareTo(r) <= 0;
      }
  }

  @Override
  public void exitComparisonExpressionWithOperator(TransformationParser.ComparisonExpressionWithOperatorContext ctx) {
    String op = ctx.getChild(1).getText();
    Token<?> right = popStack();
    Token<?> left = popStack();
    if(left.getValue() instanceof Number
    && right.getValue() instanceof Number
      ) {
      Double l = ((Number)left.getValue()).doubleValue();
      Double r = ((Number)right.getValue()).doubleValue();
      tokenStack.push(new Token<>(compare(l, r, op), Boolean.class));

    }
    else {
      String l = left.getValue() == null?"":left.getValue().toString();
      String r = right.getValue() == null?"":right.getValue().toString();
      tokenStack.push(new Token<>(compare(l, r, op), Boolean.class));
    }
  }

  @Override
  public void enterList_entity(TransformationParser.List_entityContext ctx) {
    tokenStack.push(new Token<>(new FunctionMarker(), FunctionMarker.class));
  }

  public Token<?> popStack() {
    if(tokenStack.empty()) {
      throw new ParseException("Unable to pop an empty stack");
    }
    return tokenStack.pop();
  }

  public Object getResult() throws ParseException {
    if(tokenStack.empty()) {
      throw new ParseException("Invalid predicate: Empty stack.");
    }
    Token<?> token = popStack();
    if(tokenStack.empty()) {
      return token.getValue();
    }
    if(tokenStack.empty()) {
      throw new ParseException("Invalid parse, stack not empty: " + Joiner.on(',').join(tokenStack));
    }
    else {
      throw new ParseException("Invalid parse, found " + token);
    }
  }
}
