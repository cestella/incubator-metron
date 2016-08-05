// Generated from org/apache/metron/common/transformation/generated/Transformation.g4 by ANTLR 4.5
package org.apache.metron.common.transformation.generated;

//CHECKSTYLE:OFF
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

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TransformationParser}.
 */
public interface TransformationListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TransformationParser#transformation}.
	 * @param ctx the parse tree
	 */
	void enterTransformation(TransformationParser.TransformationContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransformationParser#transformation}.
	 * @param ctx the parse tree
	 */
	void exitTransformation(TransformationParser.TransformationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonExpression}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpression(TransformationParser.ComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonExpression}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpression(TransformationParser.ComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TernaryFunc}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterTernaryFunc(TransformationParser.TernaryFuncContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TernaryFunc}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitTernaryFunc(TransformationParser.TernaryFuncContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NotFunc}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterNotFunc(TransformationParser.NotFuncContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NotFunc}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitNotFunc(TransformationParser.NotFuncContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TransformationEntity}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterTransformationEntity(TransformationParser.TransformationEntityContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TransformationEntity}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitTransformationEntity(TransformationParser.TransformationEntityContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithExpression}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterArithExpression(TransformationParser.ArithExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithExpression}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitArithExpression(TransformationParser.ArithExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TransformationExpr}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterTransformationExpr(TransformationParser.TransformationExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TransformationExpr}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitTransformationExpr(TransformationParser.TransformationExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LogicalExpressionAnd}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpressionAnd(TransformationParser.LogicalExpressionAndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LogicalExpressionAnd}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpressionAnd(TransformationParser.LogicalExpressionAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LogicalExpressionOr}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpressionOr(TransformationParser.LogicalExpressionOrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LogicalExpressionOr}
	 * labeled alternative in {@link TransformationParser#transformation_expr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpressionOr(TransformationParser.LogicalExpressionOrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonExpressionWithOperator}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpressionWithOperator(TransformationParser.ComparisonExpressionWithOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonExpressionWithOperator}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpressionWithOperator(TransformationParser.ComparisonExpressionWithOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InExpression}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void enterInExpression(TransformationParser.InExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InExpression}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void exitInExpression(TransformationParser.InExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NInExpression}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void enterNInExpression(TransformationParser.NInExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NInExpression}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void exitNInExpression(TransformationParser.NInExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonExpressionParens}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpressionParens(TransformationParser.ComparisonExpressionParensContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonExpressionParens}
	 * labeled alternative in {@link TransformationParser#comparison_expr}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpressionParens(TransformationParser.ComparisonExpressionParensContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IdentifierOperand}
	 * labeled alternative in {@link TransformationParser#comparison_operand}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierOperand(TransformationParser.IdentifierOperandContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IdentifierOperand}
	 * labeled alternative in {@link TransformationParser#comparison_operand}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierOperand(TransformationParser.IdentifierOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransformationParser#transformation_entity}.
	 * @param ctx the parse tree
	 */
	void enterTransformation_entity(TransformationParser.Transformation_entityContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransformationParser#transformation_entity}.
	 * @param ctx the parse tree
	 */
	void exitTransformation_entity(TransformationParser.Transformation_entityContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonOp}
	 * labeled alternative in {@link TransformationParser#comp_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOp(TransformationParser.ComparisonOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonOp}
	 * labeled alternative in {@link TransformationParser#comp_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOp(TransformationParser.ComparisonOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithOp_plus}
	 * labeled alternative in {@link TransformationParser#arith_operator_addition}.
	 * @param ctx the parse tree
	 */
	void enterArithOp_plus(TransformationParser.ArithOp_plusContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithOp_plus}
	 * labeled alternative in {@link TransformationParser#arith_operator_addition}.
	 * @param ctx the parse tree
	 */
	void exitArithOp_plus(TransformationParser.ArithOp_plusContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithOp_mul}
	 * labeled alternative in {@link TransformationParser#arith_operator_mul}.
	 * @param ctx the parse tree
	 */
	void enterArithOp_mul(TransformationParser.ArithOp_mulContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithOp_mul}
	 * labeled alternative in {@link TransformationParser#arith_operator_mul}.
	 * @param ctx the parse tree
	 */
	void exitArithOp_mul(TransformationParser.ArithOp_mulContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransformationParser#func_args}.
	 * @param ctx the parse tree
	 */
	void enterFunc_args(TransformationParser.Func_argsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransformationParser#func_args}.
	 * @param ctx the parse tree
	 */
	void exitFunc_args(TransformationParser.Func_argsContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransformationParser#op_list}.
	 * @param ctx the parse tree
	 */
	void enterOp_list(TransformationParser.Op_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransformationParser#op_list}.
	 * @param ctx the parse tree
	 */
	void exitOp_list(TransformationParser.Op_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransformationParser#list_entity}.
	 * @param ctx the parse tree
	 */
	void enterList_entity(TransformationParser.List_entityContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransformationParser#list_entity}.
	 * @param ctx the parse tree
	 */
	void exitList_entity(TransformationParser.List_entityContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithExpr_solo}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr}.
	 * @param ctx the parse tree
	 */
	void enterArithExpr_solo(TransformationParser.ArithExpr_soloContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithExpr_solo}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr}.
	 * @param ctx the parse tree
	 */
	void exitArithExpr_solo(TransformationParser.ArithExpr_soloContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithExpr_plus}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr}.
	 * @param ctx the parse tree
	 */
	void enterArithExpr_plus(TransformationParser.ArithExpr_plusContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithExpr_plus}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr}.
	 * @param ctx the parse tree
	 */
	void exitArithExpr_plus(TransformationParser.ArithExpr_plusContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithExpr_minus}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr}.
	 * @param ctx the parse tree
	 */
	void enterArithExpr_minus(TransformationParser.ArithExpr_minusContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithExpr_minus}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr}.
	 * @param ctx the parse tree
	 */
	void exitArithExpr_minus(TransformationParser.ArithExpr_minusContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithExpr_div}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr_mul}.
	 * @param ctx the parse tree
	 */
	void enterArithExpr_div(TransformationParser.ArithExpr_divContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithExpr_div}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr_mul}.
	 * @param ctx the parse tree
	 */
	void exitArithExpr_div(TransformationParser.ArithExpr_divContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithExpr_mul_solo}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr_mul}.
	 * @param ctx the parse tree
	 */
	void enterArithExpr_mul_solo(TransformationParser.ArithExpr_mul_soloContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithExpr_mul_solo}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr_mul}.
	 * @param ctx the parse tree
	 */
	void exitArithExpr_mul_solo(TransformationParser.ArithExpr_mul_soloContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithExpr_mul}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr_mul}.
	 * @param ctx the parse tree
	 */
	void enterArithExpr_mul(TransformationParser.ArithExpr_mulContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithExpr_mul}
	 * labeled alternative in {@link TransformationParser#arithmetic_expr_mul}.
	 * @param ctx the parse tree
	 */
	void exitArithExpr_mul(TransformationParser.ArithExpr_mulContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TransformationFunc}
	 * labeled alternative in {@link TransformationParser#functions}.
	 * @param ctx the parse tree
	 */
	void enterTransformationFunc(TransformationParser.TransformationFuncContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TransformationFunc}
	 * labeled alternative in {@link TransformationParser#functions}.
	 * @param ctx the parse tree
	 */
	void exitTransformationFunc(TransformationParser.TransformationFuncContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumericFunctions}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void enterNumericFunctions(TransformationParser.NumericFunctionsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumericFunctions}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void exitNumericFunctions(TransformationParser.NumericFunctionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code DoubleLiteral}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void enterDoubleLiteral(TransformationParser.DoubleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code DoubleLiteral}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void exitDoubleLiteral(TransformationParser.DoubleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntLiteral}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void enterIntLiteral(TransformationParser.IntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntLiteral}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void exitIntLiteral(TransformationParser.IntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void enterVariable(TransformationParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Variable}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void exitVariable(TransformationParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenArith}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void enterParenArith(TransformationParser.ParenArithContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenArith}
	 * labeled alternative in {@link TransformationParser#arithmetic_operands}.
	 * @param ctx the parse tree
	 */
	void exitParenArith(TransformationParser.ParenArithContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LogicalConst}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void enterLogicalConst(TransformationParser.LogicalConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LogicalConst}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void exitLogicalConst(TransformationParser.LogicalConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithmeticOperands}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticOperands(TransformationParser.ArithmeticOperandsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithmeticOperands}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticOperands(TransformationParser.ArithmeticOperandsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringLiteral}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(TransformationParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringLiteral}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(TransformationParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code List}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void enterList(TransformationParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by the {@code List}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void exitList(TransformationParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExistsFunc}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void enterExistsFunc(TransformationParser.ExistsFuncContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExistsFunc}
	 * labeled alternative in {@link TransformationParser#identifier_operand}.
	 * @param ctx the parse tree
	 */
	void exitExistsFunc(TransformationParser.ExistsFuncContext ctx);
}