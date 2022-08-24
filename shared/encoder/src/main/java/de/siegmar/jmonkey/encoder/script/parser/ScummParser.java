/*
 * JMonkey - Java based development kit for "The Secret of Monkey Island"
 * Copyright (C) 2022  Oliver Siegmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.siegmar.jmonkey.encoder.script.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.siegmar.jmonkey.encoder.script.parser.statement.AssignmentExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.BinaryExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.BooleanLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.CallExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.EvalExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Expression;
import de.siegmar.jmonkey.encoder.script.parser.statement.ExpressionStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.GotoStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.Identifier;
import de.siegmar.jmonkey.encoder.script.parser.statement.IncDecExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.LabeledStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.LiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.MemberExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.NumericLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;
import de.siegmar.jmonkey.encoder.script.parser.statement.Statement;
import de.siegmar.jmonkey.encoder.script.parser.statement.StringLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.UnaryExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.UnlessStatement;

@SuppressWarnings({
    "checkstyle:SummaryJavadoc",
    "checkstyle:JavadocStyle",
    "checkstyle:JavadocMethod"
})
public class ScummParser {

    private final ScummTokenizer tokenizer;
    private Token lookahead;

    public ScummParser(final ScummTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Program parse() {
        return program();
    }

    /**
     * <pre>{@code
     * Program
     *   : StatementList
     *   ;
     * }</pre>
     */
    private Program program() {
        lookahead = tokenizer.next();
        return Program.of(statementList());
    }

    /**
     * <pre>{@code
     * StatementList
     *   : Statement
     *   | StatementList Statement
     *   ;
     * }</pre>
     */
    private List<Statement> statementList() {
        final List<Statement> statementList = new ArrayList<>();
        final Statement statement = statement();
        statementList.add(statement);

        while (lookahead != null && lookahead.getType() != null) {
            statementList.add(statement());
        }

        return statementList;
    }

    /**
     * <pre>{@code
     * Statement
     *   : ExpressionStatement
     *   | UnlessStatement
     *   | GotoStatement
     *   ;
     * }</pre>
     */
    private Statement statement() {
        return switch (lookahead.getType()) {
            case UNLESS -> unlessStatement();
            case GOTO -> gotoStatement();
            case LABEL -> labelStatement();
            default -> expressionStatement();
        };
    }

    private Statement labelStatement() {
        final Token token = eat(TokenType.LABEL);
        final String value = token.getValue();

        return LabeledStatement.of(Identifier.of(value.substring(0, value.length() - 1)), statement());
    }

    /**
     * <pre>{@code
     * UnlessStatement
     *   : 'unless' '(' Expression ')' GotoStatement
     *   ;
     * }</pre>
     */
    private UnlessStatement unlessStatement() {
        eat(TokenType.UNLESS);
        eat(TokenType.LPAREN);
        final Expression expression = expression();
        eat(TokenType.RPAREN);
        final GotoStatement consequence = gotoStatement();
        return UnlessStatement.of(expression, consequence);
    }

    /**
     * <pre>{@code
     * GotoStatement
     *   : 'goto' Identifier ';'
     *   ;
     * }</pre>
     */
    private GotoStatement gotoStatement() {
        eat(TokenType.GOTO);
        final Identifier identifier = identifier();
        eat(TokenType.SEMICOLON);
        return GotoStatement.of(identifier);
    }

    /**
     * <pre>{@code
     * ExpressionStatement
     *   : Expression ';'
     *   | LabeledStatement ExpressionStatement
     *   ;
     * }</pre>
     */
    private Statement expressionStatement() {
        final Expression expression = expression();

        eat(TokenType.SEMICOLON);
        return ExpressionStatement.of(expression);
    }

    /**
     * <pre>{@code
     * Expression
     *   : AssignmentExpression
     *   ;
     * }</pre>
     */
    private Expression expression() {
        return assignmentExpression();
    }

    /**
     * <pre>{@code
     * AssignmentExpression
     *   : EqualityExpression
     *   | IncDecExpression
     *   | LeftHandSideExpression AssignmentOperator AssignmentExpression
     *   ;
     * }</pre>
     */
    private Expression assignmentExpression() {
        final Expression left = equalityExpression();

        if (lookahead.getType() == TokenType.INCREMENT
            || lookahead.getType() == TokenType.DECREMENT) {
            return incDecExpression(left);
        }

        if (lookahead.getType() != TokenType.SIMPLE_ASSIGN
            && lookahead.getType() != TokenType.COMPLEX_ASSIGN) {
            return left;
        }

        return AssignmentExpression.of(assignmentOperator().getValue(),
            checkValidAssignmentTarget(left),
            assignmentExpression());
    }

    private Expression incDecExpression(final Expression left) {
        final Token token = eat(lookahead.getType());
        return IncDecExpression.of(token.getValue(), left);
    }

    /**
     * <pre>{@code
     * EqualityExpression
     *   : RelationalExpression
     *   | EqualityExpression EQUALITY_OPERATOR RelationalExpression
     *   ;
     * }</pre>
     */
    private Expression equalityExpression() {
        return binaryExpression(TokenType.EQUALITY_OPERATOR, this::relationalExpression);
    }

    /**
     * <pre>{@code
     * RelationalExpression
     *   : AdditiveExpression
     *   : RelationalExpression RELATIONAL_OPERATOR AdditiveExpression
     *   ;
     * }</pre>
     */
    private Expression relationalExpression() {
        return binaryExpression(TokenType.RELATIONAL_OPERATOR, this::additiveExpression);
    }

    /**
     * <pre>{@code
     * UnaryExpression
     *   : LeftHandSideExpression
     *   | ADDITIVE_OPERATOR UnaryExpression
     *   | LOGICAL_NOT UnaryExpression
     *   ;
     * }</pre>
     */
    private Expression unaryExpression() {
        final String operator = switch (lookahead.getType()) {
            case ADDITIVE_OPERATOR -> eat(TokenType.ADDITIVE_OPERATOR).getValue();
            case LOGICAL_NOT -> eat(TokenType.LOGICAL_NOT).getValue();
            default -> null;
        };
        if (operator != null) {
            return UnaryExpression.of(operator, unaryExpression());
        }

        return leftHandSideExpression();
    }

    /**
     * <pre>{@code
     * LeftHandSideExpression
     *   : CallMemberExpression
     *   ;
     * }</pre>
     */
    private Expression leftHandSideExpression() {
        return callMemberExpression();
    }

    /**
     * <pre>{@code
     * CallMemberExpression
     *   : MemberExpression
     *   | CallExpression
     *   ;
     * }</pre>
     */
    private Expression callMemberExpression() {
        final Expression member = memberExpression();

        if (lookahead.getType() == TokenType.LPAREN) {
            return callExpression(member);
        }

        return member;
    }

    /**
     * <pre>{@code
     * Generic call expression helper.
     *
     *  CallExpression
     *    : Callee Arguments
     *    ;
     *
     *  Callee
     *    : MemberExpression
     *    | Super
     *    | CallExpression
     *    ;
     * }</pre>
     */
    private Expression callExpression(final Expression member) {
        return CallExpression.of(member, arguments());
    }

    /**
     * <pre>{@code
     * Arguments
     *   : '(' OptArgumentList ')'
     *   ;
     * }</pre>
     */
    private List<Expression> arguments() {
        eat(TokenType.LPAREN);

        final List<Expression> argumentList = lookahead.getType() != TokenType.RPAREN ? argumentList() : null;

        eat(TokenType.RPAREN);

        return argumentList;
    }

    /**
     * <pre>{@code
     * ArgumentList
     *   : AssignmentExpression
     *   | ArgumentList ',' AssignmentExpression
     *   ;
     * }</pre>
     */
    private List<Expression> argumentList() {
        final List<Expression> args = new ArrayList<>();

        do {
            args.add(assignmentExpression());
        } while (lookahead.getType() == TokenType.COMMA && eat(TokenType.COMMA) != null);

        return args;
    }

    /**
     * <pre>{@code
     * MemberExpression
     *   : PrimaryExpression
     *   | MemberExpression '.' Identifier
     *   | MemberExpression '[' AssignmentExpression ']'
     * }</pre>
     */
    private Expression memberExpression() {
        Expression expression = primaryExpression();

        while (lookahead.getType() == TokenType.DOT || lookahead.getType() == TokenType.LSPAREN) {
            if (lookahead.getType() == TokenType.DOT) {
                eat(TokenType.DOT);
                expression = MemberExpression.of(false, expression, identifier());
            }
            if (lookahead.getType() == TokenType.LSPAREN) {
                eat(TokenType.LSPAREN);

                while (lookahead.getType() != TokenType.RSPAREN) {
                    expression = MemberExpression.of(true, expression, assignmentExpression());
                }

                eat(TokenType.RSPAREN);
            }
        }

        return expression;
    }

    /**
     * <pre>{@code
     * Identifier
     *   : IDENTIFIER
     *   ;
     * }</pre>
     */
    private Identifier identifier() {
        return Identifier.of(eat(TokenType.IDENTIFIER).getValue());
    }

    private Expression checkValidAssignmentTarget(final Expression left) {
        if (left instanceof Identifier || left instanceof MemberExpression) {
            return left;
        }
        throw new IllegalStateException("Invalid left-hand side in assignment expression");
    }

    /**
     * <pre>{@code
     * AssignmentOperator
     *   : SIMPLE_ASSIGN
     *   | COMPLEX_ASSIGN
     *   ;
     * }</pre>
     */
    private Token assignmentOperator() {
        return switch (lookahead.getType()) {
            case SIMPLE_ASSIGN -> eat(TokenType.SIMPLE_ASSIGN);
            case COMPLEX_ASSIGN -> eat(TokenType.COMPLEX_ASSIGN);
            default -> throw new IllegalStateException("Unexpected value: " + lookahead.getType());
        };
    }

    /**
     * <pre>{@code
     * AdditiveExpression
     *   : MultiplicativeExpression
     *   | AdditiveExpression ADDITIVE_OPERATOR MultiplicativeExpression
     *   ;
     * }</pre>
     */
    private Expression additiveExpression() {
        return binaryExpression(TokenType.ADDITIVE_OPERATOR, this::multiplicativeExpression);
    }

    private Expression binaryExpression(final TokenType tokenType, final Supplier<Expression> fnc) {
        Expression left = fnc.get();

        while (lookahead.getType() == tokenType) {
            final Token operator = eat(tokenType);
            final Expression right = fnc.get();

            left = BinaryExpression.of(operator.getValue(), left, right);
        }

        return left;
    }

    /**
     * <pre>{@code
     * MultiplicativeExpression
     *   : UnaryExpression
     *   | MultiplicativeExpression MULTIPLICATIVE_OPERATOR UnaryExpression
     *   ;
     * }</pre>
     */
    private Expression multiplicativeExpression() {
        return binaryExpression(TokenType.MULTIPLICATIVE_OPERATOR, this::unaryExpression);
    }

    /**
     * <pre>{@code
     * PrimaryExpression
     *   : Literal
     *   | ParenthesizedExpression
     *   | Identifier
     *   ;
     * }</pre>
     */
    private Expression primaryExpression() {
        if (isLiteral(lookahead.getType())) {
            return literal();
        }
        return switch (lookahead.getType()) {
            case LPAREN -> parenthesizedExpression();
            case IDENTIFIER -> identifier();
            case EVAL -> eval();
            default -> throw new IllegalStateException("Unexpected primary expression");
        };
    }

    private Expression eval() {
        eat(TokenType.EVAL);
        final EvalExpression eval = EvalExpression.of(expression());
        eat(TokenType.EVAL);
        return eval;
    }

    /**
     * <pre>{@code
     * ParenthesizedExpression
     *   | '(' Expression ')'
     *   ;
     * }</pre>
     */
    private Expression parenthesizedExpression() {
        eat(TokenType.LPAREN);
        final Expression expression = expression();
        eat(TokenType.RPAREN);
        return expression;
    }

    private boolean isLiteral(final TokenType type) {
        return type == TokenType.NUMBER || type == TokenType.STRING
            || type == TokenType.TRUE || type == TokenType.FALSE;
    }

    /**
     * <pre>{@code
     * Literal
     *   : StringLiteral
     *   : NumericLiteral
     *   | BooleanLiteral
     *   ;
     * }</pre>
     */
    private LiteralExpression literal() {
        return switch (lookahead.getType()) {
            case NUMBER -> numericLiteral();
            case STRING -> stringLiteral();
            case TRUE -> booleanLiteral(true);
            case FALSE -> booleanLiteral(false);
            default -> throw new IllegalStateException("Unknown literal type: " + lookahead.getType());
        };
    }

    /**
     * <pre>{@code
     * BooleanLiteral
     *   : 'true'
     *   | 'false'
     *   ;
     * }</pre>
     */
    private LiteralExpression booleanLiteral(final boolean value) {
        eat(value ? TokenType.TRUE : TokenType.FALSE);
        return BooleanLiteralExpression.of(value);
    }

    /**
     * <pre>{@code
     * StringLiteral
     *   : STRING
     *   ;
     * }</pre>
     */
    private StringLiteralExpression stringLiteral() {
        final String str = eat(TokenType.STRING).getValue();
        return StringLiteralExpression.of(str.substring(1, str.length() - 1));
    }

    /**
     * <pre>{@code
     * NumericLiteral
     *   : NUMBER
     *   ;
     * }</pre>
     */
    private NumericLiteralExpression numericLiteral() {
        return NumericLiteralExpression.of(Integer.valueOf(eat(TokenType.NUMBER).getValue()));
    }

    private Token eat(final TokenType type) {
        final Token token = lookahead;
        if (token == null) {
            throw new IllegalStateException("Unexpected end of input, expected: " + type);
        }

        if (token.getType() != type) {
            throw new IllegalStateException("Unexpected token: " + token.getType() + "; expected: " + type);
        }

        lookahead = tokenizer.next();

        return token;
    }

}
