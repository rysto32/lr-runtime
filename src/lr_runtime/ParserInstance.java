/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lr_runtime;

import java.util.LinkedList;

/**
 *
 * @author rstone
 */
class ParserInstance {
    private final Parser<ParseStackActions> parser;
    private final Parser<StateStackActions> stateParser;
    private final ParseStackActions parserStack;
    private final LrParser lrParser;
    
    ParserInstance(Scanner lex, TokenFactory factory, LrParser lrParser) {
        LinkedList<Token> defered = new LinkedList<Token>();
        parserStack = new ParseStackActions(lrParser, factory);
        
        parser = new Parser<ParseStackActions>(parserStack,
                    new RepairParseTable(lrParser.table));
        
        CheckpointScanner scanner = new CheckpointScanner(lex);
        StateStackActions stateStack = new StateStackActions(lrParser, defered, parser, scanner, factory);
        
        stateParser = new Parser<StateStackActions>(stateStack, lrParser.table);
        this.lrParser = lrParser;
    }

    private ParserInstance(Parser<ParseStackActions> parser,
            Parser<StateStackActions> stateParser, ParseStackActions parserStack,
            LrParser lrParser) {
        this.parser = parser;
        this.stateParser = stateParser;
        this.parserStack = parserStack;
        this.lrParser = lrParser;
    }
    
    ParserInstance branch() {
        ParseStackActions newParserStack = parserStack.branch();
        Parser<ParseStackActions> newParser = new Parser<ParseStackActions>(parserStack, lrParser.table);
        return new ParserInstance(newParser, stateParser.branch(), newParserStack, lrParser);
    }
    
    Action.Performed nextToken(Token lookahead) {
        return stateParser.nextToken(lookahead);
    }
    
    Object getStackTop() {
        return parserStack.getStackTop();
    }
}
