package org.apache.metron.common.transformation;

import com.google.common.base.Joiner;
import org.apache.metron.common.query.FunctionMarker;
import org.apache.metron.common.query.ParseException;
import org.apache.metron.common.query.StringFunctions;
import org.apache.metron.common.query.VariableResolver;
import org.apache.metron.common.transformation.generated.TransformationBaseListener;
import org.apache.metron.common.transformation.generated.TransformationParser;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public class TransformationCompiler extends TransformationBaseListener {
  private VariableResolver resolver = null;
  private Stack<TransformationToken> tokenStack = new Stack<>();
  public TransformationCompiler(VariableResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void enterTransformation(TransformationParser.TransformationContext ctx) {
    tokenStack.clear();
  }


  @Override
  public void exitVariable(TransformationParser.VariableContext ctx) {
    tokenStack.push(new TransformationToken<>(resolver.resolve(ctx.getText()), Object.class));
  }

  @Override
  public void exitStringLiteral(TransformationParser.StringLiteralContext ctx) {
    tokenStack.push(new TransformationToken<>(ctx.getText().substring(1, ctx.getText().length() - 1), String.class));
  }


  @Override
  public void exitIntegerLiteral(TransformationParser.IntegerLiteralContext ctx) {
    tokenStack.push(new TransformationToken<>(Integer.parseInt(ctx.getText()), Integer.class));
  }


  @Override
  public void exitDoubleLiteral(TransformationParser.DoubleLiteralContext ctx) {
    tokenStack.push(new TransformationToken<>(Double.parseDouble(ctx.getText()), Double.class));
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
    TransformationToken<?> left = popStack();
    List<Object> argList = null;
    if(left.getUnderlyingType().equals(List.class)) {
      argList = (List<Object>) left.getValue();
    }
    else {
      throw new ParseException("Unable to process in clause because " + left.getValue() + " is not a set");
    }
    Object result = func.apply(argList);
    tokenStack.push(new TransformationToken<>(result, Object.class));
  }


  @Override
  public void enterFunc_args(TransformationParser.Func_argsContext ctx) {
    tokenStack.push(new TransformationToken<>(new FunctionMarker(), FunctionMarker.class));
  }


  @Override
  public void exitFunc_args(TransformationParser.Func_argsContext ctx) {
    LinkedList<Object> args = new LinkedList<>();
    while(true) {
      TransformationToken<?> token = popStack();
      if(token.getUnderlyingType().equals(FunctionMarker.class)) {
        break;
      }
      else {
        args.addFirst(token.getValue());
      }
    }
    tokenStack.push(new TransformationToken<>(args, List.class));
  }


  @Override
  public void exitList_entity(TransformationParser.List_entityContext ctx) {
    LinkedList<Object> args = new LinkedList<>();
    while(true) {
      TransformationToken<?> token = popStack();
      if(token.getUnderlyingType().equals(FunctionMarker.class)) {
        break;
      }
      else {
        args.addFirst(token.getValue());
      }
    }
    tokenStack.push(new TransformationToken<>(args, List.class));
  }

  @Override
  public void enterList_entity(TransformationParser.List_entityContext ctx) {
    tokenStack.push(new TransformationToken<>(new FunctionMarker(), FunctionMarker.class));
  }

  public TransformationToken<?> popStack() {
    if(tokenStack.empty()) {
      throw new ParseException("Unable to pop an empty stack");
    }
    return tokenStack.pop();
  }

  public Object getResult() throws ParseException {
    if(tokenStack.empty()) {
      throw new ParseException("Invalid predicate: Empty stack.");
    }
    TransformationToken<?> token = popStack();
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
