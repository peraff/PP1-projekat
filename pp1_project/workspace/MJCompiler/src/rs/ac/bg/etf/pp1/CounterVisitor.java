package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.FormalParamDecl;
import rs.ac.bg.etf.pp1.ast.FormalParsDeclNiz;
import rs.ac.bg.etf.pp1.ast.FormalParsDeclVar;
import rs.ac.bg.etf.pp1.ast.VarDecl;
import rs.ac.bg.etf.pp1.ast.VarDeclaration;
import rs.ac.bg.etf.pp1.ast.VarDeclarationSquare;
import rs.ac.bg.etf.pp1.ast.VarTypeDeclaration;
import rs.ac.bg.etf.pp1.ast.VarTypeDeclarationSquare;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class CounterVisitor extends VisitorAdaptor {
	protected int count; 
	
	public int getCount() { 
		return count;
	}
	
	public static class FormParamCounter extends CounterVisitor { 
		public void visit(FormalParsDeclNiz formParamDecl) { 
			count ++;
		}
		public void visit(FormalParsDeclVar formParamDecl) { 
			count ++;
		}
	}
	
	public static class VarCounter extends CounterVisitor { 
		public void visit(VarDeclarationSquare varDecl) { 
			count ++;
		}
		
		public void visit(VarDeclaration varDecl) { 
			count ++;
		}
		
		public void visit(VarTypeDeclarationSquare varDecl) { 
			count ++;
		}
		
		public void visit(VarTypeDeclaration varDecl) { 
			count ++;
		}
		
	}
}
