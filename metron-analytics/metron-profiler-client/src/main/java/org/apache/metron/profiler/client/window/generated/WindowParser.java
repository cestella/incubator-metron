// Generated from org/apache/metron/profiler/client/window/generated/Window.g4 by ANTLR 4.5
package org.apache.metron.profiler.client.window.generated;

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

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class WindowParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		COMMA=1, BIN=2, INCLUDE=3, EXCLUDE=4, NOW=5, FROM=6, EVERY=7, TO=8, AGO=9, 
		NUMBER=10, WS=11, DAY_SPECIFIER=12, TIME_UNIT=13;
	public static final int
		RULE_window = 0, RULE_window_expression = 1, RULE_excluding_specifier = 2, 
		RULE_including_specifier = 3, RULE_specifier = 4, RULE_specifier_list = 5, 
		RULE_duration = 6, RULE_skip_distance = 7, RULE_bin_width = 8, RULE_time_interval = 9, 
		RULE_time_amount = 10, RULE_time_unit = 11;
	public static final String[] ruleNames = {
		"window", "window_expression", "excluding_specifier", "including_specifier", 
		"specifier", "specifier_list", "duration", "skip_distance", "bin_width", 
		"time_interval", "time_amount", "time_unit"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "COMMA", "BIN", "INCLUDE", "EXCLUDE", "NOW", "FROM", "EVERY", "TO", 
		"AGO", "NUMBER", "WS", "DAY_SPECIFIER", "TIME_UNIT"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Window.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public WindowParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class WindowContext extends ParserRuleContext {
		public Window_expressionContext window_expression() {
			return getRuleContext(Window_expressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(WindowParser.EOF, 0); }
		public WindowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_window; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterWindow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitWindow(this);
		}
	}

	public final WindowContext window() throws RecognitionException {
		WindowContext _localctx = new WindowContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_window);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(24);
			window_expression();
			setState(25);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Window_expressionContext extends ParserRuleContext {
		public Window_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_window_expression; }
	 
		public Window_expressionContext() { }
		public void copyFrom(Window_expressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RepeatingWindowContext extends Window_expressionContext {
		public Bin_widthContext bin_width() {
			return getRuleContext(Bin_widthContext.class,0);
		}
		public Skip_distanceContext skip_distance() {
			return getRuleContext(Skip_distanceContext.class,0);
		}
		public DurationContext duration() {
			return getRuleContext(DurationContext.class,0);
		}
		public Including_specifierContext including_specifier() {
			return getRuleContext(Including_specifierContext.class,0);
		}
		public Excluding_specifierContext excluding_specifier() {
			return getRuleContext(Excluding_specifierContext.class,0);
		}
		public RepeatingWindowContext(Window_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterRepeatingWindow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitRepeatingWindow(this);
		}
	}
	public static class NonRepeatingWindowContext extends Window_expressionContext {
		public Bin_widthContext bin_width() {
			return getRuleContext(Bin_widthContext.class,0);
		}
		public Including_specifierContext including_specifier() {
			return getRuleContext(Including_specifierContext.class,0);
		}
		public Excluding_specifierContext excluding_specifier() {
			return getRuleContext(Excluding_specifierContext.class,0);
		}
		public NonRepeatingWindowContext(Window_expressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterNonRepeatingWindow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitNonRepeatingWindow(this);
		}
	}

	public final Window_expressionContext window_expression() throws RecognitionException {
		Window_expressionContext _localctx = new Window_expressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_window_expression);
		int _la;
		try {
			setState(43);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new NonRepeatingWindowContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(27);
				bin_width();
				setState(29);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(28);
					including_specifier();
					}
					break;
				}
				setState(32);
				_la = _input.LA(1);
				if (_la==DAY_SPECIFIER) {
					{
					setState(31);
					excluding_specifier();
					}
				}

				}
				break;
			case 2:
				_localctx = new RepeatingWindowContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(34);
				bin_width();
				setState(35);
				skip_distance();
				setState(36);
				duration();
				setState(38);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(37);
					including_specifier();
					}
					break;
				}
				setState(41);
				_la = _input.LA(1);
				if (_la==DAY_SPECIFIER) {
					{
					setState(40);
					excluding_specifier();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Excluding_specifierContext extends ParserRuleContext {
		public Excluding_specifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_excluding_specifier; }
	 
		public Excluding_specifierContext() { }
		public void copyFrom(Excluding_specifierContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ExcludingContext extends Excluding_specifierContext {
		public Specifier_listContext specifier_list() {
			return getRuleContext(Specifier_listContext.class,0);
		}
		public ExcludingContext(Excluding_specifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterExcluding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitExcluding(this);
		}
	}

	public final Excluding_specifierContext excluding_specifier() throws RecognitionException {
		Excluding_specifierContext _localctx = new Excluding_specifierContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_excluding_specifier);
		try {
			_localctx = new ExcludingContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			specifier_list(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Including_specifierContext extends ParserRuleContext {
		public Including_specifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_including_specifier; }
	 
		public Including_specifierContext() { }
		public void copyFrom(Including_specifierContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IncludingContext extends Including_specifierContext {
		public Specifier_listContext specifier_list() {
			return getRuleContext(Specifier_listContext.class,0);
		}
		public IncludingContext(Including_specifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterIncluding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitIncluding(this);
		}
	}

	public final Including_specifierContext including_specifier() throws RecognitionException {
		Including_specifierContext _localctx = new Including_specifierContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_including_specifier);
		try {
			_localctx = new IncludingContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			specifier_list(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SpecifierContext extends ParserRuleContext {
		public SpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specifier; }
	 
		public SpecifierContext() { }
		public void copyFrom(SpecifierContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DaySpecifierContext extends SpecifierContext {
		public TerminalNode DAY_SPECIFIER() { return getToken(WindowParser.DAY_SPECIFIER, 0); }
		public DaySpecifierContext(SpecifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterDaySpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitDaySpecifier(this);
		}
	}

	public final SpecifierContext specifier() throws RecognitionException {
		SpecifierContext _localctx = new SpecifierContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_specifier);
		try {
			_localctx = new DaySpecifierContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			match(DAY_SPECIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Specifier_listContext extends ParserRuleContext {
		public SpecifierContext specifier() {
			return getRuleContext(SpecifierContext.class,0);
		}
		public Specifier_listContext specifier_list() {
			return getRuleContext(Specifier_listContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(WindowParser.COMMA, 0); }
		public Specifier_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specifier_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterSpecifier_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitSpecifier_list(this);
		}
	}

	public final Specifier_listContext specifier_list() throws RecognitionException {
		return specifier_list(0);
	}

	private Specifier_listContext specifier_list(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Specifier_listContext _localctx = new Specifier_listContext(_ctx, _parentState);
		Specifier_listContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_specifier_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(52);
			specifier();
			}
			_ctx.stop = _input.LT(-1);
			setState(59);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Specifier_listContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_specifier_list);
					setState(54);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(55);
					match(COMMA);
					setState(56);
					specifier();
					}
					} 
				}
				setState(61);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class DurationContext extends ParserRuleContext {
		public DurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_duration; }
	 
		public DurationContext() { }
		public void copyFrom(DurationContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FromToDurationContext extends DurationContext {
		public TerminalNode FROM() { return getToken(WindowParser.FROM, 0); }
		public List<Time_intervalContext> time_interval() {
			return getRuleContexts(Time_intervalContext.class);
		}
		public Time_intervalContext time_interval(int i) {
			return getRuleContext(Time_intervalContext.class,i);
		}
		public TerminalNode TO() { return getToken(WindowParser.TO, 0); }
		public List<TerminalNode> AGO() { return getTokens(WindowParser.AGO); }
		public TerminalNode AGO(int i) {
			return getToken(WindowParser.AGO, i);
		}
		public FromToDurationContext(DurationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterFromToDuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitFromToDuration(this);
		}
	}
	public static class ToDurationContext extends DurationContext {
		public TerminalNode TO() { return getToken(WindowParser.TO, 0); }
		public Time_intervalContext time_interval() {
			return getRuleContext(Time_intervalContext.class,0);
		}
		public TerminalNode AGO() { return getToken(WindowParser.AGO, 0); }
		public ToDurationContext(DurationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterToDuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitToDuration(this);
		}
	}

	public final DurationContext duration() throws RecognitionException {
		DurationContext _localctx = new DurationContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_duration);
		int _la;
		try {
			setState(77);
			switch (_input.LA(1)) {
			case FROM:
				_localctx = new FromToDurationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(62);
				match(FROM);
				setState(63);
				time_interval();
				setState(65);
				_la = _input.LA(1);
				if (_la==AGO) {
					{
					setState(64);
					match(AGO);
					}
				}

				setState(67);
				match(TO);
				setState(68);
				time_interval();
				setState(70);
				_la = _input.LA(1);
				if (_la==AGO) {
					{
					setState(69);
					match(AGO);
					}
				}

				}
				break;
			case TO:
				_localctx = new ToDurationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(72);
				match(TO);
				setState(73);
				time_interval();
				setState(75);
				_la = _input.LA(1);
				if (_la==AGO) {
					{
					setState(74);
					match(AGO);
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Skip_distanceContext extends ParserRuleContext {
		public Skip_distanceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_skip_distance; }
	 
		public Skip_distanceContext() { }
		public void copyFrom(Skip_distanceContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SkipDistanceContext extends Skip_distanceContext {
		public TerminalNode EVERY() { return getToken(WindowParser.EVERY, 0); }
		public Time_intervalContext time_interval() {
			return getRuleContext(Time_intervalContext.class,0);
		}
		public SkipDistanceContext(Skip_distanceContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterSkipDistance(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitSkipDistance(this);
		}
	}

	public final Skip_distanceContext skip_distance() throws RecognitionException {
		Skip_distanceContext _localctx = new Skip_distanceContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_skip_distance);
		try {
			_localctx = new SkipDistanceContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(EVERY);
			setState(80);
			time_interval();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bin_widthContext extends ParserRuleContext {
		public Bin_widthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bin_width; }
	 
		public Bin_widthContext() { }
		public void copyFrom(Bin_widthContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class BinWidthContext extends Bin_widthContext {
		public Time_intervalContext time_interval() {
			return getRuleContext(Time_intervalContext.class,0);
		}
		public TerminalNode BIN() { return getToken(WindowParser.BIN, 0); }
		public BinWidthContext(Bin_widthContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterBinWidth(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitBinWidth(this);
		}
	}

	public final Bin_widthContext bin_width() throws RecognitionException {
		Bin_widthContext _localctx = new Bin_widthContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_bin_width);
		try {
			_localctx = new BinWidthContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			time_interval();
			setState(83);
			match(BIN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Time_intervalContext extends ParserRuleContext {
		public Time_intervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_time_interval; }
	 
		public Time_intervalContext() { }
		public void copyFrom(Time_intervalContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TimeIntervalContext extends Time_intervalContext {
		public Time_amountContext time_amount() {
			return getRuleContext(Time_amountContext.class,0);
		}
		public Time_unitContext time_unit() {
			return getRuleContext(Time_unitContext.class,0);
		}
		public TimeIntervalContext(Time_intervalContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterTimeInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitTimeInterval(this);
		}
	}

	public final Time_intervalContext time_interval() throws RecognitionException {
		Time_intervalContext _localctx = new Time_intervalContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_time_interval);
		try {
			_localctx = new TimeIntervalContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			time_amount();
			setState(86);
			time_unit();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Time_amountContext extends ParserRuleContext {
		public Time_amountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_time_amount; }
	 
		public Time_amountContext() { }
		public void copyFrom(Time_amountContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TimeAmountContext extends Time_amountContext {
		public TerminalNode NUMBER() { return getToken(WindowParser.NUMBER, 0); }
		public TimeAmountContext(Time_amountContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterTimeAmount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitTimeAmount(this);
		}
	}

	public final Time_amountContext time_amount() throws RecognitionException {
		Time_amountContext _localctx = new Time_amountContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_time_amount);
		try {
			_localctx = new TimeAmountContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Time_unitContext extends ParserRuleContext {
		public Time_unitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_time_unit; }
	 
		public Time_unitContext() { }
		public void copyFrom(Time_unitContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TimeUnitContext extends Time_unitContext {
		public TerminalNode TIME_UNIT() { return getToken(WindowParser.TIME_UNIT, 0); }
		public TimeUnitContext(Time_unitContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).enterTimeUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WindowListener ) ((WindowListener)listener).exitTimeUnit(this);
		}
	}

	public final Time_unitContext time_unit() throws RecognitionException {
		Time_unitContext _localctx = new Time_unitContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_time_unit);
		try {
			_localctx = new TimeUnitContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			match(TIME_UNIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 5:
			return specifier_list_sempred((Specifier_listContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean specifier_list_sempred(Specifier_listContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\17_\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\3\2\3\2\3\2\3\3\3\3\5\3 \n\3\3\3\5\3#\n\3\3\3\3\3\3\3\3"+
		"\3\5\3)\n\3\3\3\5\3,\n\3\5\3.\n\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\7\7<\n\7\f\7\16\7?\13\7\3\b\3\b\3\b\5\bD\n\b\3\b\3\b\3\b"+
		"\5\bI\n\b\3\b\3\b\3\b\5\bN\n\b\5\bP\n\b\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3"+
		"\13\3\13\3\f\3\f\3\r\3\r\3\r\2\3\f\16\2\4\6\b\n\f\16\20\22\24\26\30\2"+
		"\2\\\2\32\3\2\2\2\4-\3\2\2\2\6/\3\2\2\2\b\61\3\2\2\2\n\63\3\2\2\2\f\65"+
		"\3\2\2\2\16O\3\2\2\2\20Q\3\2\2\2\22T\3\2\2\2\24W\3\2\2\2\26Z\3\2\2\2\30"+
		"\\\3\2\2\2\32\33\5\4\3\2\33\34\7\2\2\3\34\3\3\2\2\2\35\37\5\22\n\2\36"+
		" \5\b\5\2\37\36\3\2\2\2\37 \3\2\2\2 \"\3\2\2\2!#\5\6\4\2\"!\3\2\2\2\""+
		"#\3\2\2\2#.\3\2\2\2$%\5\22\n\2%&\5\20\t\2&(\5\16\b\2\')\5\b\5\2(\'\3\2"+
		"\2\2()\3\2\2\2)+\3\2\2\2*,\5\6\4\2+*\3\2\2\2+,\3\2\2\2,.\3\2\2\2-\35\3"+
		"\2\2\2-$\3\2\2\2.\5\3\2\2\2/\60\5\f\7\2\60\7\3\2\2\2\61\62\5\f\7\2\62"+
		"\t\3\2\2\2\63\64\7\16\2\2\64\13\3\2\2\2\65\66\b\7\1\2\66\67\5\n\6\2\67"+
		"=\3\2\2\289\f\3\2\29:\7\3\2\2:<\5\n\6\2;8\3\2\2\2<?\3\2\2\2=;\3\2\2\2"+
		"=>\3\2\2\2>\r\3\2\2\2?=\3\2\2\2@A\7\b\2\2AC\5\24\13\2BD\7\13\2\2CB\3\2"+
		"\2\2CD\3\2\2\2DE\3\2\2\2EF\7\n\2\2FH\5\24\13\2GI\7\13\2\2HG\3\2\2\2HI"+
		"\3\2\2\2IP\3\2\2\2JK\7\n\2\2KM\5\24\13\2LN\7\13\2\2ML\3\2\2\2MN\3\2\2"+
		"\2NP\3\2\2\2O@\3\2\2\2OJ\3\2\2\2P\17\3\2\2\2QR\7\t\2\2RS\5\24\13\2S\21"+
		"\3\2\2\2TU\5\24\13\2UV\7\4\2\2V\23\3\2\2\2WX\5\26\f\2XY\5\30\r\2Y\25\3"+
		"\2\2\2Z[\7\f\2\2[\27\3\2\2\2\\]\7\17\2\2]\31\3\2\2\2\f\37\"(+-=CHMO";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}