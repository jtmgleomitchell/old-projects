package RegAlloc;

import Graph.*;
import FlowGraph.*;
import Temp.*;

/***********************************************************************************************************
 *   CLASSE Liveness                                                                                       *
 * ------------------------------------------------------------------------------------------------------- *
 *   Faz an�lise do liveness de cada tempor�rio do programa e constr�i um grafo de interfer�ncias (este    *
 *   grafo possui uma aresta entre cada par de tempor�rios que est�o vivos no mesmo momento, indicando     *
 *   que n�o podem estar no mesmo registrador.                                                             *
 ***********************************************************************************************************/

public class Liveness extends InterferenceGraph
{
  // armazena todos os tempor�rios que est�o vivos ap�s o n� (instru��o assembly) associado (live-out)
  private java.util.Dictionary liveOutMap = new java.util.Hashtable();
  // mapeia tempor�rios para n�s no grafo de interfer�ncia e vice-versa
  private java.util.Dictionary tempToNodeMap = new java.util.Hashtable();
  private java.util.Dictionary nodeToTempMap = new java.util.Hashtable();
  // associa a cada tempor�rio o n�mero de vezes em que � usado em todo o programa
  private java.util.Dictionary useOfTemps = new java.util.Hashtable();
  // lista com todos os moves presentes
  private MoveList moveList = null;

  /* construtor Liveness (AssemFlowGraph flow)
   *    realiza a an�lise do liveness e a contru��o do grafo de interfer�ncias a partir do grafo de fluxo
   *    do programa.
   */
  public Liveness(AssemFlowGraph flow) {
    makeLiveMap(flow);
    buildInterferenceGraph(flow);
    countNumberOfUses(flow);
  }

  /* makeLiveMap (AssemFlowGraph flow)
   *    faz a an�lise do liveness de cada tempor�rio a partir do grafo de fluxo do programa; como resultado,
   *    indica, ap�s cada instru��o assembly, quais os tempor�rios que est�o vivos.
   */
  private void makeLiveMap (AssemFlowGraph flow) {
    // armazena todos os tempor�rios que est�o vivos ao chegar a um n� (live-in)
    java.util.Dictionary liveInMap = new java.util.Hashtable();
    // associa a cada instru��o assembly o conjunto dos tempor�rios que s�o usados e os que s�o definidos
    java.util.Dictionary uses = new java.util.Hashtable();
    java.util.Dictionary defs = new java.util.Hashtable();
    
    // para cada instru��o assembly, cria um TempSet com os tempor�rios que s�o usados e outro com os que
    // s�o definidos
    for (NodeList nl=flow.nodes(); nl!=null; nl=nl.tail) {
      liveInMap.put(nl.head,new TempSet());
      liveOutMap.put(nl.head,new TempSet());
      defs.put(nl.head,new TempSet(flow.def(nl.head)));
      uses.put(nl.head,new TempSet(flow.use(nl.head)));
    }

    boolean changed;    // indica se houve mudan�a nos conjuntos live-in e/ou live-out do algum n�
    NodeList flowNodes = revert(flow.nodes());   // "inverte" os n�s do grafo de fluxo para acelerar o
                                                 // processo de liveness
    do {
      changed = false;
      // percorre todos os n�s (instru��es assembly) do grafo
      for (NodeList nodes=flowNodes; nodes!=null; nodes=nodes.tail) {
	Node n = nodes.head;
	// obt�m os conjuntos com os tempor�rios que est�o vivos ao entrar e ao sair da instru��o assembly
	TempSet oldIn = (TempSet) liveInMap.get(n);
	TempSet oldOut = (TempSet) liveOutMap.get(n);
	TempSet use = (TempSet) uses.get(n);
	TempSet def = (TempSet) defs.get(n);
	// atualiza o conjunto dos tempor�rios que est�o vivos ao sair deste n�
	TempSet out = new TempSet();
	for (NodeList s=n.succ(); s!=null; s=s.tail) {
	  TempSet in_s = (TempSet) liveInMap.get(s.head);
	  out = out.union(in_s);
	}
	liveOutMap.put (n, out);
	// atualiza o conjunto dos tempor�rios que est�o vivos ao chegar a este n�
	TempSet in = use.union(out.difference(def));
	liveInMap.put (n, in);
	// verifica se houve mudan�as
	changed = changed || (!in.isEqualTo(oldIn) || !out.isEqualTo(oldOut));
      }
    } while (changed);
  }

  /* buildInterferenceGraph (AssemFlowGraph flow)
   *    com as informa��es sobre o liveness de cada tempor�rio e a partir do grafo de fluxo do programa,
   *    constr�i o grafop de interfer�ncias.
   */
  private void buildInterferenceGraph (AssemFlowGraph flow) {
    for (NodeList nodes=flow.nodes(); nodes!=null; nodes=nodes.tail) {
      Assem.Instr instruct = flow.instr(nodes.head);
 
      if (instruct instanceof Assem.MOVE) {
	// Para cada MOVE a <- c,  onde os tempor�rios b1 ... bj est�o live-out, ser� gerada uma aresta
	// de interfer�ncia (a,b1) .. (a,bj) para todo bk que n�o seja o mesmo que a.
	Assem.MOVE move = (Assem.MOVE) instruct;
	Node node1 = tnode(move.dst);
	moveList = new MoveList (tnode(move.src), node1, moveList);
	TempSet out = (TempSet)liveOutMap.get(nodes.head);
	for (Temp t=out.getFirst(); t!=null; t=out.getNext(t))
	  if (t != move.src && t != move.dst) {
	    Node node2 = tnode(t);
	    addEdge (node1, node2);
	  }
      }
      else {
	// Para cada opera��o que n�o for MOVE que defina um tempor�rio a, cujos tempor�rios live-out sejam
	// b1 ... bj, ser� gerada uma aresta de interfer�ncia (a,b1) ... (a,bj)
	for (TempList t=flow.def(nodes.head); t!=null; t=t.tail) {
	  Node node1 = tnode(t.head);
	  TempSet out = (TempSet)liveOutMap.get(nodes.head);
	  for (Temp u=out.getFirst(); u!=null; u=out.getNext(u))
	    if (t.head != u) {
	      Node node2 = tnode(u);
	      addEdge (node1, node2);
	    }
	}
      }
    }
  }

  /* countNumberOfUses (AssemFlowGraph flow)
   *    conta o n�mero de vezez em que cada tempor�rio � utilizado em todo o programa.
   */
  private void countNumberOfUses (AssemFlowGraph flow) {
    for (NodeList nl=nodes(); nl!=null; nl=nl.tail) {
      Temp t = gtemp(nl.head);
      useOfTemps.put(t,new Integer(0));
    }
    for (NodeList il=flow.nodes(); il!=null; il=il.tail) {
      Assem.Instr instruct = flow.instr(il.head);
      for (TempList ul=instruct.use(); ul!=null; ul=ul.tail) {
	  // FIXME: n pode ser null
	Integer n = (Integer)useOfTemps.get(ul.head);
	//if (n == null) 
	useOfTemps.put(ul.head, new Integer(n.intValue()+1));
      }
    }
  }
    
  /* Node tnode (Temp temp)
   *    devolve o n� associado a um tempor�rio no grafo de interfer�ncias.
   */
  public Node tnode (Temp temp) {
    Node n = (Node) tempToNodeMap.get(temp);
    if (n == null) {
      n = newNode();
      tempToNodeMap.put(temp,n);
      nodeToTempMap.put(n,temp);
    }
    return n;
  }

  /* Temp gtemp (Node node)
   *    devolve o tempor�rio associado a um n� do grafo de interfer�ncias.
   */
  public Temp gtemp (Node node) {
    return (Temp) nodeToTempMap.get(node);
  }

  /* MoveList moves()
   *    retorna uma lista com todos os MOVEs do programa.
   */
  public MoveList moves() {
    return moveList;
  }

  /* show (java.io.PrintStream out)
   *    grava em out uma representa��o deste grafo de interfer�ncias.
   */
  public void show(java.io.PrintStream out) {
    for (NodeList p=nodes(); p!=null; p=p.tail) {
      Node n = p.head;
      out.print(gtemp(n));
      out.print(": ");
      for(NodeList q=n.adj(); q!=null; q=q.tail) {
	out.print(gtemp(q.head));
	out.print(" ");
      }
      out.println();
    }
  }

  /* int spillCost (Node n)
   *    retorna o custo associado a fazer "spill" do n� (tempor�rio) fornecido.
   */
  public int spillCost (Node n) {
    // leva em conta o n�mero de vezes que o tempor�rio � usado e o grau deste tempor�rio no grafo de
    // interfer�ncias
    int uses = ((Integer)useOfTemps.get(gtemp(n))).intValue();
    return 10000*uses / n.degree();
  }

  /* int numberOfUses (Temp t)
   *    retorna o n�mero de vezes que um tempor�rio � usado.
   */
  public int numberOfUses (Temp t) {
    return ((Integer)useOfTemps.get(t)).intValue();
  }

  /* NodeList revert (NodeList nl)
   *    cria uma lista invertida a partir de uma outra lista.
   */
  private NodeList revert (NodeList nl) {
    NodeList x = null;
    for ( ; nl!=null; nl=nl.tail)
      x = new NodeList (nl.head, x);
    return x;
  }
}
