package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.test.CompilerError;
import rs.ac.bg.etf.pp1.test.CompilerError.CompilerErrorType;
import java.util.List;
import java.util.ArrayList;
%%

%{
	
	public List<CompilerError> llist = new ArrayList<CompilerError>();
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}
	

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n" 	{ }
"\f" 	{ }

"program"   { return new_symbol(sym.PROG, yytext()); }
"else"      {return new_symbol(sym.ELSE, yytext()); }
"const"     {return new_symbol(sym.CONST, yytext()); }
"if" 		{return new_symbol(sym.IF, yytext()); }
"new" 		{return new_symbol(sym.NEW, yytext()); }
"read" 		{return new_symbol(sym.READ, yytext()); }
"print" 	{ return new_symbol(sym.PRINT, yytext()); }
"void" 		{ return new_symbol(sym.VOID, yytext()); }
"+" 		{ return new_symbol(sym.PLUS, yytext()); }
"-" 		{ return new_symbol(sym.MINUS, yytext()); }
"*" 		{ return new_symbol(sym.STAR, yytext()); }
"/" 		{ return new_symbol(sym.DASH, yytext()); }
"%" 		{ return new_symbol(sym.PERCENT, yytext()); }
"==" 		{ return new_symbol(sym.EQU, yytext()); }
"!=" 		{ return new_symbol(sym.NOTEQU, yytext()); }
">" 		{ return new_symbol(sym.GREATER, yytext()); }
">=" 		{ return new_symbol(sym.GREATERE, yytext()); }
"<" 		{ return new_symbol(sym.LESS, yytext()); }
"<=" 		{ return new_symbol(sym.LESSE, yytext()); }
"&&" 		{ return new_symbol(sym.AND, yytext()); }
"||" 		{ return new_symbol(sym.OR, yytext()); }
"=" 		{ return new_symbol(sym.EQUAL, yytext()); }
"++" 		{ return new_symbol(sym.PLUSPLUS, yytext()); }
"--" 		{ return new_symbol(sym.MINUSMINUS, yytext()); }
";" 		{ return new_symbol(sym.SEMI, yytext()); }
"," 		{ return new_symbol(sym.COMMA, yytext()); }
"(" 		{ return new_symbol(sym.LPAREN, yytext()); }
")" 		{ return new_symbol(sym.RPAREN, yytext()); }
"{" 		{ return new_symbol(sym.LBRACE, yytext()); }
"}"			{ return new_symbol(sym.RBRACE, yytext()); }
"[" 		{ return new_symbol(sym.LSQUARE, yytext()); }
"]" 		{ return new_symbol(sym.RSQUARE, yytext()); }

<YYINITIAL> "//" 		     { yybegin(COMMENT); }
<COMMENT> .     			 { yybegin(COMMENT); }
<COMMENT> "\r\n" 			 { yybegin(YYINITIAL); }

<YYINITIAL>"'"."'" { return new_symbol (sym.CONSTCHAR, new Character(yytext().charAt(1)) ); }
"true"|"false" {return new_symbol (sym.CONSTBOOL, new Boolean(yytext().equals("true") ? true : false)); } 
[0-9]+  { return new_symbol(sym.NUMBER, new Integer (yytext())); }
([a-z]|[A-Z])[a-zA-Z0-9_]* 	{return new_symbol (sym.IDENT, yytext()); }

. { System.out.println("Leksicka greska ("+yytext()+") u liniji "+(yyline+1));
	llist.add(new CompilerError(yyline+1, "Leksicka greska ("+yytext()+ ")", CompilerErrorType.LEXICAL_ERROR)); }






