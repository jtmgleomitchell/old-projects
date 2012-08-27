package FlowGraph;

import Graph.*;
import Temp.*;
import Assem.*;


/***********************************************************************************************************
 *   CLASSE AssemFlowGraph                                                                                 *
 * ------------------------------------------------------------------------------------------------------- *
 *   Cria um grafo representando o fluxo de um conjunto de instru��es.                                     *
 ***********************************************************************************************************/

public class AssemFlowGraph extends FlowGraph
{
  // esta tabela associar� r�tulos com n�s (onde estes r�tulos s�o definidos)
  private java.util.Dictionary labelsTable = new java.util.Hashtable();
  // esta tabela associar� instru��es assembly a n�s
  private java.util.Dictionary instrTable = new java.util.Hashtable();

  /* construtor AssemFlowGraph (InstrList instrs)
   *    a partir de um conjunto de instru��es, cria um grafo representando o fluxo do programa; para isso,
   *    utilliza a informa��o sobre poss�veis desvios que cada instru��o assembly pode realizar.
   */
  public AssemFlowGraph (InstrList instrs) {
    Node last = null;
    for ( ; instrs!=null; instrs=instrs.tail) {
      Node node;
      // obt�m um n� para esta instru��o
      if (instrs.head instanceof LABEL)
	node = getNodeDefiningLabel(((LABEL)instrs.head).label);
      else
	node = newNode();
      instrTable.put(node,instrs.head);
      // cria uma aresta do n� da instru��o anterior para o n� da atual
      if (last != null)
	addEdge(last,node);
      last = node;
      // verifica se esta instru��o assembly faz desvio para algum r�tulo
      if (instrs.head instanceof OPER && ((OPER)instrs.head).jumps()!=null) {
	// cria uma aresta entre este n� e todos aqueles para o qual a instru��o assembly pode desviar
	OPER oper = (OPER) instrs.head;
	for (LabelList ll=oper.jumps().labels; ll!=null; ll=ll.tail)
	  addEdge (node, getNodeDefiningLabel(ll.head));
	last = null;
      }
    }
  }

  /* Node getNodeDefiningLabel (Label label)
   *    devolve o n� associado � instru��o que faz a declara��o do r�tulo especificado (esta declara��o pode
   *    n�o estar presente no conjunto de instru��es assembly que est�o sendo analisadas).
   */
  private Node getNodeDefiningLabel (Label label) {
    Node node = (Node) labelsTable.get(label);
    if (node == null) {
      // cria um n� representando onde o r�tulo foi declarado
      node = newNode();
      labelsTable.put(label, node);
    }
    return node;
  }

  /* TempList def (Node n)
   *    retorna o conjunto com os tempor�rios definidos por esta instru��o assembly (n�).
   */
  public TempList def (Node n) {
    Instr instr = ((Instr)instrTable.get(n));
    if (instr!=null)
      return instr.def();
    return null;
  }

  /* TempList def (Node n)
   *    retorna o conjunto com os tempor�rios usados por esta instru��o assembly (n�).
   */
  public TempList use (Node n){
    Instr instr = ((Instr)instrTable.get(n));
    if (instr!=null)
      return instr.use();
    return null;
  }

  /* boolean isMove (Node n)
   *    verifica se este n� est� associado a uma instru��o MOVE.
   */
  public boolean isMove (Node n) {
    return ((Instr)instrTable.get(n)) instanceof MOVE;
  }

  /* Instr instr (Node n)
   *    retorna a instru��o assembly associada a este n�.
   */
  public Instr instr (Node n) {
    return (Instr) instrTable.get(n);
  }
}
