package org.apache.metron.threatintel.triage.predicate;

import com.google.common.base.Function;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.apache.metron.threatintel.triage.generated.*;

import java.util.*;

public class PredicateProcessor {
  private static class FunctionMarker { }
  private interface BooleanOp {
    boolean op(boolean left, boolean right);
  }
  private static class TreeBuilder extends PredicateBaseListener {
    private VariableResolver resolver = null;
    private Stack<PredicateToken> tokenStack = new Stack<>();

    public TreeBuilder(VariableResolver resolver) {
      this.resolver = resolver;
    }

    @Override
    public void enterSingle_rule(org.apache.metron.threatintel.triage.generated.PredicateParser.Single_ruleContext ctx) {
      tokenStack.clear();
    }

    @Override
    public void exitSingle_rule(org.apache.metron.threatintel.triage.generated.PredicateParser.Single_ruleContext ctx) {
    }

    @Override
    public void exitLogicalExpressionAnd(PredicateParser.LogicalExpressionAndContext ctx) {
      PredicateToken<?> left = tokenStack.pop();
      PredicateToken<?> right = tokenStack.pop();
      tokenStack.push(new PredicateToken<>(booleanOp(left, right, (l, r) -> l && r, "&&"), Boolean.class));
    }

    @Override
    public void exitLogicalExpressionOr(PredicateParser.LogicalExpressionOrContext ctx) {
      PredicateToken<?> left = tokenStack.pop();
      PredicateToken<?> right = tokenStack.pop();

      tokenStack.push(new PredicateToken<>(booleanOp(left, right, (l, r) -> l || r, "||"), Boolean.class));
    }

    private boolean booleanOp(PredicateToken<?> left, PredicateToken<?> right, BooleanOp op, String opName)
    {
      if(left.getUnderlyingType().equals(right.getUnderlyingType()) && left.getUnderlyingType().equals(Boolean.class)) {
        Boolean l = (Boolean) left.getValue();
        Boolean r = (Boolean) right.getValue();
        if(l == null || r == null) {
          throw new RuntimeException("Unable to operate on " + left.getValue()  + " " + opName + " " + right.getValue() + ", null value");
        }
        return op.op(l, r);
      }
      else {
        throw new RuntimeException("Unable to operate on " + left.getValue()  + " " + opName + " " + right.getValue() + ", bad types");
      }
    }


    @Override
    public void exitLogicalConst(PredicateParser.LogicalConstContext ctx) {
      Boolean b = null;
      switch(ctx.getText().toUpperCase()) {
        case "TRUE":
          b = true;
          break;
        case "FALSE":
          b = false;
          break;
        default:
          throw new RuntimeException("Unable to process " + ctx.getText() + " as a boolean constant");
      }
      tokenStack.push(new PredicateToken<>(b, Boolean.class));
    }

    @Override
    public void exitComparisonExpressionWithOperator(PredicateParser.ComparisonExpressionWithOperatorContext ctx) {
      boolean isEqualsOp = ctx.getChild(1).getText().equals("==");
      PredicateToken<?> left = tokenStack.pop();
      PredicateToken<?> right = tokenStack.pop();
      if(left.getUnderlyingType().equals(right.getUnderlyingType())) {
        boolean isEquals = left.equals(right);
        tokenStack.push(new PredicateToken<>(isEqualsOp?isEquals:!isEquals, Boolean.class));
      }
      else {
        throw new RuntimeException("Unable to compare " + left.getValue() + " " + ctx.getText() + " " + right.getValue());
      }
    }


    @Override
    public void exitLogicalVariable(PredicateParser.LogicalVariableContext ctx) {
      tokenStack.push(new PredicateToken<>(resolver.resolve(ctx.getText()), String.class));
    }


    @Override
    public void exitStringLiteral(PredicateParser.StringLiteralContext ctx) {
      String val = ctx.getText();
      tokenStack.push(new PredicateToken<>(val.substring(1, val.length() - 1), String.class));
    }


    @Override
    public void enterList_entity(PredicateParser.List_entityContext ctx) {
      tokenStack.push(new PredicateToken<>(new FunctionMarker(), FunctionMarker.class));
    }


    @Override
    public void exitList_entity(PredicateParser.List_entityContext ctx) {
      Set<String> inSet = new HashSet<>();
      while(true) {
        PredicateToken<?> token = tokenStack.pop();
        if(token.getUnderlyingType().equals(FunctionMarker.class)) {
          break;
        }
        else {
          inSet.add((String)token.getValue());
        }
      }
      tokenStack.push(new PredicateToken<>(inSet, Set.class));
    }


    @Override
    public void enterFunc_args(PredicateParser.Func_argsContext ctx) {
      tokenStack.push(new PredicateToken<>(new FunctionMarker(), FunctionMarker.class));
    }


    @Override
    public void exitFunc_args(PredicateParser.Func_argsContext ctx) {
      List<String> args = new ArrayList<>();
      while(true) {
        PredicateToken<?> token = tokenStack.pop();
        if(token.getUnderlyingType().equals(FunctionMarker.class)) {
          break;
        }
        else {
          args.add((String)token.getValue());
        }
      }
      tokenStack.push(new PredicateToken<>(args, List.class));
    }

    @Override
    public void exitInExpression(PredicateParser.InExpressionContext ctx) {
      PredicateToken<?> left = tokenStack.pop();
      PredicateToken<?> right = tokenStack.pop();
      String key = null;
      Set<String> set = null;
      if(left.getUnderlyingType().equals(Set.class)) {
        set = (Set<String>) left.getValue();
      }
      else {
        throw new RuntimeException("Unable to process in clause because " + left.getValue() + " is not a set");
      }
      if(right.getUnderlyingType().equals(String.class)) {
        key = (String) right.getValue();
      }
      else {
        throw new RuntimeException("Unable to process in clause because " + right.getValue() + " is not a string");
      }
      tokenStack.push(new PredicateToken<>(set.contains(key), Boolean.class));
    }

    @Override
    public void exitNInExpression(PredicateParser.NInExpressionContext ctx) {
      PredicateToken<?> left = tokenStack.pop();
      PredicateToken<?> right = tokenStack.pop();
      String key = null;
      Set<String> set = null;
      if(left.getUnderlyingType().equals(Set.class)) {
        set = (Set<String>) left.getValue();
      }
      else {
        throw new RuntimeException("Unable to process in clause because " + left.getValue() + " is not a set");
      }
      if(right.getUnderlyingType().equals(String.class)) {
        key = (String) right.getValue();
      }
      else {
        throw new RuntimeException("Unable to process in clause because " + right.getValue() + " is not a string");
      }
      tokenStack.push(new PredicateToken<>(!set.contains(key), Boolean.class));
    }

    @Override
    public void exitStringFunc(PredicateParser.StringFuncContext ctx) {
      String funcName = ctx.getChild(0).getText();
      Function<List<String>, String> func = StringFunctions.valueOf(funcName);
      PredicateToken<?> left = tokenStack.pop();
      List<String> argList = null;
      if(left.getUnderlyingType().equals(List.class)) {
        argList = (List<String>) left.getValue();
      }
      else {
        throw new RuntimeException("Unable to process in clause because " + left.getValue() + " is not a set");
      }
      String result = func.apply(argList);
      tokenStack.push(new PredicateToken<>(result, String.class));
    }

    @Override
    public void exitExistsFunc(PredicateParser.ExistsFuncContext ctx) {
      String variable = ctx.getChild(2).getText();
      boolean exists = resolver.resolve(variable) != null;
      tokenStack.push(new PredicateToken<>(exists, Boolean.class));
    }

    @Override
    public void exitLogicalFunc(PredicateParser.LogicalFuncContext ctx) {
    }

    public boolean getResult() {
      if(tokenStack.empty()) {
        throw new RuntimeException("Invalid predicate: Empty stack.");
      }
      PredicateToken<?> token = tokenStack.pop();
      if(token.getUnderlyingType().equals(Boolean.class)) {
        return (Boolean)token.getValue();
      }
      throw new RuntimeException("Invalid parse, found " + token + " but expected boolean");
    }
  }
  public boolean parse(String rule, VariableResolver resolver) {
    ANTLRInputStream input = new ANTLRInputStream(rule);
    PredicateLexer lexer = new PredicateLexer(input);
    TokenStream tokens = new CommonTokenStream(lexer);
    PredicateParser parser = new PredicateParser(tokens);

    TreeBuilder treeBuilder = new TreeBuilder(resolver);
    parser.addParseListener(treeBuilder);
    parser.single_rule();
    return treeBuilder.getResult();
  }
}
