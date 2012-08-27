@i portugues.w

@* SAFER. Esta � a primeira fase do projeto de implementa��o do algoritmo
de criptografia chamado SAFER K-64 (Secure And Fast Encryption Routine),
desenvolvido por J.~Massey para a empresa Cylink Corporation, que utiliza
chaves de $64$ bits para criptografia.

O algoritmo baseia-se na repeti��o de uma determinada seq��ncia de opera��es
chamada {\it round} para efetuar a criptografia da entrada. Para uma boa
margem de seguran�a, recomenda-se que sejam executados pelo menos $6$ rounds
para criptografar um dado arquivo. Cada round do algoritmo � uma seq��ncia de
$6$ opera��es, cuja descri��o precisa veremos adiante.

O algoritmo SAFER ser� programado tendo-se em mente arquiteturas com bytes de
$8$ bits. Em particular, como as chaves tem tamanho $64$ bits, elas ocupam
tamanho de $8$ bytes. A seguir, temos a estrutura de nosso programa:

@c

@<Defini��es do Pr�-Processador@>@;
@<Fun��es Auxiliares@>@;
int main(int argc, char *argv[])
{
  @<Vari�veis@>@;
  @<Inicializa��o@>@;
  @<Algoritmo SAFER@>@;
  @<Finaliza��o@>@;
  return 0;
}

@ Cada round de nosso programa consiste de uma seq��ncia de $6$
opera��es b�sicas, sendo que duas chaves s�o utilizadas por cada round
(mais especificamente, as chaves ser�o necess�rias no primeiro e terceiro
 passos do round).

O algoritmo SAFER opera sobre blocos de $8$ bytes, isto �, a entrada �
dividida em blocos de $8$ bytes. Uma macro do compilador chamada |BLKSIZE|
``conter�'' este valor.  Nossa decis�o neste programa para criptografar
arquivos cujos tamanhos n�o sejam m�ltiplos de $8$ bytes � a de simplesmente
adicionar espa�os ao fim do arquivo at� que seu tamanho seja um m�ltiplo de
$8$.  Abaixo apresentamos a rotina de leitura da entrada. A fun��o |leitura|
recebe dois par�metros, |fp| e |v|. O par�metro |fp| � um ponteiro para uma
estrutura de arquivo de entrada, enquanto o par�metro |v| � um ponteiro para
uma string de caracteres. A fun��o devolve como sa�da o n�mero de bytes lidos
da entrada e os bytes lidos na string apontada por |v| (caso a entrada acabe
antes de |BLKSIZE| bytes serem lidos, |v| ser� preenchida com os espa�os em
branco).

@<Fun...@>=

int leitura(FILE *fp, unsigned char *v)
{
  register int i = 0, j, c;

  while (((c = getc(fp)) != EOF) && (i < BLKSIZE)) {
    v[i] = c;
    i++;
  }

  if (c != EOF) /* um caracter a mais do que devia foi pego */
    ungetc(c, fp);

  for (j = i; j < BLKSIZE; j++)
    v[j] = ' '; /* preenchendo com brancos */

  return i;
}

@ Fun��o de escrita.
@<Fun...@>=
int escrita(FILE *fp, unsigned char *v, int n)
{
  register int i = 0, erro = 0;

  while ((i < n) && (!erro)) {
    erro = (putc(v[i], fp) == EOF);
    i++;
  }

  return !erro;
}

@ Antes de que n�s nos esque�amos, daremos algumas declara��es do
pr�-processador, definindo duas macros, |BLKSIZE| e |KEYSIZE|.
@<Def...@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define BLKSIZE 8
#define KEYSIZE BLKSIZE

@ A sintaxe do programa � a seguinte: \.{safer <entrada> <saida> <chave> [rounds]},
onde:
\item{$\bullet$} \.{entrada} � o nome do arquivo de entrada;
\item{$\bullet$} \.{saida} � o nome do arquivo de sa�da;
\item{$\bullet$} \.{chave} � uma chave alfanum�rica de comprimento exatamente
$8$ bytes;
\item{$\bullet$} \.{rounds} � um par�metro opcional, num�rico, indicando quantos
rounds do algoritmo ser�o executados por bloco de $8$ bytes do arquivo de entrada.

Para decodifica��o, a sintaxe �: \.{saferdec <entrada> <saida> <chave> [rounds]}, onde:
\item{$\bullet$} \.{entrada} � o nome do arquivo a j� codificado;
\item{$\bullet$} \.{saida} � o nome do arquivo resultante da decodifica��o;
\item{$\bullet$} \.{chave} � a mesma chave especificada no momento da codifica��o do
arquivo \.{entrada}.
\item{$\bullet$} \.{rounds} � um par�metro opcional, num�rico, indicando quantos
rounds do algoritmo ser�o executados por bloco de $8$ bytes do arquivo de entrada.

@ A inicializa��o do programa consiste em tratamento de par�metros da linha de
comando e inicializa��o de algumas vari�veis.
@<Ini...@>=
@<Tratamento de Linha de Comando@>@;
@<Pr�-c�lculo de Vari�veis@>@;

@ Aqui segue o tratamento dos par�metros de entrada que o programa deve
receber (nome de arquivos e chave) da linha de comando.
@<Tra...@>=
if ((argc != 4) && (argc != 5)) {
  fprintf(stderr,
	"Numero invalido de parametros.\nSintaxe: %s <entrada> <saida> <chave> [rounds]\n",
	argv[0]);
  exit(1);
}
else { /* o n�mero de par�metros est� correto */
  if (strstr(argv[0], "saferdec") != 0) { /* devemos decodificar */
    codifica = 0;
    printf("%s: decodificando arquivo de entrada...\n", argv[0]);
  }
  else printf("%s: codificando arquivo de entrada...\n", argv[0]);

  if (!(entrada = fopen(argv[1], "rb"))) {
    fprintf(stderr, "%s: erro abrindo arquivo de entrada\n", argv[0]);
    exit(1);
  }
  
  if (!(saida = fopen(argv[2], "wb"))) {
    fprintf(stderr, "%s: erro abrindo arquivo de saida\n", argv[0]);
    exit(1);
  }
  
  if (strlen(argv[3]) != KEYSIZE) {
    fprintf(stderr, "%s: chave deve ter exatamente %d bytes.\n", argv[0], KEYSIZE);
    exit(1);
  }

  if (argc == 5) { /* foi especificado um n�mero de rounds para o algoritmo */
    if (sscanf(argv[4], "%d", &rounds) != 1) { /* problemas lendo par�metro */
      fprintf(stderr,
              "%s: erro lendo rounds da linha de comando. Usando %d rounds...\n", argv[0], ROUNDS);
    }
    else if (rounds <= 5) {
      fprintf(stderr, "%s: aviso -- poucos rounds especificados\n", argv[0]);
    }
  }
}

@ Tendo-se em vista a constante |ROUNDS| usada na passagem anterior, devemos
declar�-la ao pr�-processador.
@<Def...@>=
#define ROUNDS 6 /* valor m�nimo recomendado, por raz�es de seguran�a */

@ Declararemos tamb�m as vari�veis |rounds| e |codifica|. A vari�vel |rounds|
ser� inicializada com o valor default |ROUNDS|, para o caso de o usu�rio n�o
declarar nenhum n�mero de rounds na linha de comando. A vari�vel |codifica|
serve apenas para indicar se devemos criptografar ou descriptografar o arquivo
dado. Ela � inicializada com o valor default $1$, significando que o programa
ir� codificar o arquivo da entrada.
@<Var...@>=
int rounds = ROUNDS, codifica = 1;

@ O pr�ximo passo com que devemos nos preocupar � com a inicializa��o das vari�veis
do programa e com as fun��es pr�-calculadas.
Como $257$ � um n�mero primo, o grupo multiplicativo do corpo $GF(257)$
possui $256$ elementos, a saber $\{1, \ldots, 256\}.$ O elemento $45$ � uma
raiz primitiva de $GF(257),$ isto �, $45^i$ assume todos os valores de $\{1,
\ldots, 256\},$ quando $i$ varia de $0$ a $255$ (o grupo multiplicativo �
c�clico). Como estamos assumindo que os bytes com que trabalhamos possuem $8$
bits, eles podem assumir $256$ valores, que s�o de $0$ a $255$. Essa discuss�o
nos mostra que a fun��o $\exp: \{0, \ldots, 255\} \longrightarrow \{1, \ldots,
256\}$ dada por $\exp(i) = 45^i$ � uma bije��o (naturalmente, $\exp$ possui
uma inversa que � $\log: \{1, \ldots, 256\} \longrightarrow \{0, \ldots,
255\}$ e tal que $\log(\exp(i)) = i.$). Como essas fun��es t�m c�mputo
relativamente caro e precisaremos cham�-las com grande freq��ncia durante todo
o algoritmo, armazenaremos seus valores em dois vetores, |exp[]| e |log[]|.
@<Pr�...@>=
exp[0] = 1; log[exp[0]] = 0;
for (i = 1; i < 256; i++) {
  if (exp[i-1])
    exp[i] = (45 * exp[i-1]) % 257;
  else
    exp[i] = (45 * 256) % 257;

  log[exp[i]] = i;
}

@ Uma das tarefas auxiliares que precisamos implementar para o funcionamento
do c�digo � a rota��o de bits � esquerda de uma palavra de tamanho |BLKSIZE|
bytes. A Linguagem C fornece uma opera��o de shift de bits � esquerda de
palavras (com tamanho dependente da implementa��o). Nossa fun��o de rota��o �
geral o suficiente para funcionar com palavras de qualquer tamanho. A fun��o
|rota��o| devolve em |t| o resultado da rota��o dos bytes de |s|. O byte mais
significativo � o de posi��o $0$. O n�mero de bits que sofrer� rota��o
(especificado na macro |ROTACAO| deve ser menor ou igual a 7.
@<Fun...@>=
void rotacao(unsigned char *s, unsigned char *t)
{
    unsigned char oldbits = 0, bits = 0;
    register int i;

    for (i = BLKSIZE - 1; i >= 0; i--) {
       bits = s[i] & ((0xff >> (8 - ROTACAO)) << (8 - ROTACAO));
       t[i] = s[i] << ROTACAO;
       t[i] |= oldbits;
       oldbits = bits >> (8 - ROTACAO);
    }

    t[BLKSIZE - 1] = t[BLKSIZE - 1] | oldbits;
}

@
@<Def...@>=
#define ROTACAO 3

@ O algoritmo SAFER necessita, para cada bloco de |BLKSIZE| bytes da entrada,
de duas chaves para cada round e de mais uma chave para uma opera��o final nos
blocos.  Logo, |2*rounds+1| chaves s�o necess�rias por bloco (as chaves s�o as
mesmas para todos os blocos, sendo que a primeira chave � a fornecida pelo
usu�rio, na hora da codifica��o/decodifica��o.
Guardaremos as chaves em um vetor. Como ignoraremos sua primeira posi��o,
(para n�o usarmos a posi��o de �ndice $0$) e o n�mero de rounds � vari�vel (j�
que pode ser especificado pelo usu�rio), o vetor de chaves ser� alocado com
|2*rounds+2| posi��es.
@<Pr�...@>=
  if (!(k = malloc((2*rounds+2) * sizeof(char *)))) { /* erro de aloca��o de mem�ria */
    fprintf(stderr, "%s: erro de alocacao de memoria para as chaves. Abortando...\n", argv[0]);
    exit(1);
  }

  for (i = 0; i < 2*rounds+2; i++) { /* � poss�vel pular a primeira posi��o
  (n�o ser� utilizada), mas seremos conservadores\dots */
    if (!(k[i] = malloc(8 * sizeof(char)))) { /* erro de alocacao de mem�ria */
      fprintf(stderr, "%s: erro de alocacao de memoria para as chaves. Abortando...\n", argv[0]);
      exit(1);
    }
  }

  for (i = 0; i < 8; i++) /* copiando a chave fornecida pelo usu�rio na primeira posi��o */
      k[1][i] = argv[3][i];

  for (i = 2; i < 2*rounds+2; i++)  /* rota��o de |3| bits de |k[i-1]| em |k[i]| */
      rotacao(k[i-1], k[i]);

  for (i = 2; i < 2*rounds+2; i++)
      for (j = 0; j < KEYSIZE; j++)
	k[i][j] += exp[exp[(9*i + j + 1) % 256]]; /* o |+1| � por conta de |j| variar de $1$ a $8$ */


@ Agora definiremos algumas vari�veis que foram usadas nas se��es anteriores.
@<Var...@>=
FILE *entrada, *saida; /* arquivos de entrada e sa�da */
unsigned char exp[256], log[256]; /* vetores para pr�-c�lculo de |exp| e |log| */
register int i, j; /* contadores de uso geral */
unsigned char **k; /* o ``vetor'' das chaves */

@* O Algoritmo. Como diversos outros algoritmos de criptografia, o SAFER � um
algoritmo que se baseia em repeti��o de um bloco de passos, cada um
representando uma fun��o invers�vel. Aqui est� o esqueleto do algoritmo:

@<Alg...@>=

if (codifica) {
  fprintf(saida, "              \n"); /* espaco para o cabe�alho na sa�da */
  
  for (i = 0; i < BLKSIZE; i++) CBC[i] = 0; /* in�cio do modo CBC */
  
  lidos = 0;
  
  while((i = leitura(entrada, v))) {
    
    lidos += i; /* |lidos| cont�m o n�mero de caracteres lidos at� o momento */

    for (j = 0; j < BLKSIZE; j++) v[j] ^= CBC[j]; /* Modo CBC */
    
    for (j = 1; j <= rounds; j++) {
      @<Primeiro Passo@>@;
      @<Segundo Passo@>@;
      @<Terceiro Passo@>@;
      @<Quarto Passo@>@;
      @<Quinto Passo@>@;
      @<Sexto Passo@>@;
      for (l = 0; l < BLKSIZE; l++) v[l] = aux[l]; /* preparando para pr�xima itera��o */
    }
    
    @<Opera��o T@>@; /* opera��o final, realizada a cada bloco */
    
    j = escrita(saida, v, BLKSIZE); /* verificar resultado da opera��o */

    for (j = 0; j < BLKSIZE; j++) /* prepando para pr�xima itera��o */
      CBC[j] = v[j];
  }
  
  rewind(saida);
  fprintf(saida, "%d", lidos);
} /* codifica��o */
else {
  char buffer[BUFSIZE];
  unsigned char CBCOLD[BLKSIZE];

  for (i = 0; i < BLKSIZE; i++) CBCOLD[i] = 0; /* para o in�cio do modo CBC */

  fgets(buffer, BUFSIZE, entrada);
  sscanf(buffer, "%d", &lidos);

  while ((lidos > 0) && (leitura(entrada, v))){
  /* |lidos| aqui indica quantos caracteres ainda devem ser lidos */

    for (i = 0; i < BLKSIZE; i++) CBC[i] = v[i]; /* CBC do pr�ximo passo */

    @<Inversa da Opera��o T@>@;

    for (j = rounds; j >= 1; j--) {
      @<Inversa do Sexto Passo@>@;
      @<Inversa do Quinto Passo@>@;
      @<Inversa do Quarto Passo@>@;
      @<Inversa do Terceiro Passo@>@;
      @<Inversa do Segundo Passo@>@;
      @<Inversa do Primeiro Passo@>@;
    }

    /* des-CBCeza */

    for (i = 0; i < BLKSIZE; i++)
	v[i] ^= CBCOLD[i];

    if (lidos >= BLKSIZE) escrita(saida, v, BLKSIZE);
    else escrita(saida, v, lidos);

    lidos -= BLKSIZE; /* quantos caracteres faltam para serem escritos */

    for (i = 0; i < BLKSIZE; i++) CBCOLD[i] = CBC[i]; /* ``update'' do modo CBC */
  }

} /* decodifica��o */

@ Vari�veis.
@<Var...@>=
int lidos, l;
unsigned char v[BLKSIZE], CBC[BLKSIZE], aux[BLKSIZE];

@ Tamanho da vari�vel |buffer|.
@<Def...@>=
#define BUFSIZE 128

@ O primeiro passo do algoritmo consiste em opera��es individuais entre os
bytes do vetor lido (ap�s ser aplicada a opera��o CBC) e os bytes da primeira
chave a ser usada no round atual. A descri��o detalhada do algoritmo est�
aqui:
@<Prim...@>=
/* o vetor |k[2*j-1]| cont�m a chave usada no momento */
v[0] = v[0] ^ k[2*j-1][0];
v[1] = v[1] + k[2*j-1][1];
v[2] = v[2] + k[2*j-1][2];
v[3] = v[3] ^ k[2*j-1][3];
v[4] = v[4] ^ k[2*j-1][4];
v[5] = v[5] + k[2*j-1][5];
v[6] = v[6] + k[2*j-1][6];
v[7] = v[7] ^ k[2*j-1][7];

@ O segundo passo do algoritmo � uma aplica��o de exponenciais e logaritmos
aos bytes resultantes da primeira fase.
@<Seg...@>=
/* s�rie de exponencia��es e logaritmos */
v[0] = exp[v[0]];
v[1] = log[v[1]];
v[2] = log[v[2]];
v[3] = exp[v[3]];
v[4] = exp[v[4]];
v[5] = log[v[5]];
v[6] = log[v[6]];
v[7] = exp[v[7]];

@ O terceiro passo � an�logo ao primeiro, com exce��o de que as opera��es de
soma s�o trocadas por opera��es |^| e vice-versa e a chave usada neste momento
� a chave seguinte � chave usada no primeiro passo.
@<Ter...@>=
/* o vetor |k[2*j]| cont�m a chave usada no momento */
v[0] = v[0] + k[2*j][0];
v[1] = v[1] ^ k[2*j][1];
v[2] = v[2] ^ k[2*j][2];
v[3] = v[3] + k[2*j][3];
v[4] = v[4] + k[2*j][4];
v[5] = v[5] ^ k[2*j][5];
v[6] = v[6] ^ k[2*j][6];
v[7] = v[7] + k[2*j][7];

@ A quarta fase � resultado de um sistema linear (invers�vel) m�dulo $256$.
@<Qua...@>=
aux[0] = (2*v[0] + v[1]) % 256;
aux[1] = (2*v[2] + v[3]) % 256;
aux[2] = (2*v[4] + v[5]) % 256;
aux[3] = (2*v[6] + v[7]) % 256;
aux[4] = (v[0] + v[1]) % 256;
aux[5] = (v[2] + v[3]) % 256;
aux[6] = (v[4] + v[5]) % 256;
aux[7] = (v[6] + v[7]) % 256;

@ A quinta fase � exatamente igual ao quarto passo.
@<Qui...@>=
v[0] = (2*aux[0] + aux[1]) % 256;
v[1] = (2*aux[2] + aux[3]) % 256;
v[2] = (2*aux[4] + aux[5]) % 256;
v[3] = (2*aux[6] + aux[7]) % 256;
v[4] = (aux[0] + aux[1]) % 256;
v[5] = (aux[2] + aux[3]) % 256;
v[6] = (aux[4] + aux[5]) % 256;
v[7] = (aux[6] + aux[7]) % 256;

@ O sistema aplicado aqui � o mesmo que o anterior, mas com a diferen�a de
que os resultados n�o s�o ``tran�ados''.
@<Sex...@>=
aux[0] = (2*v[0] + v[1]) % 256;
aux[1] = (v[0] + v[1]) % 256;
aux[2] = (2*v[2] + v[3]) % 256;
aux[3] = (v[2] + v[3]) % 256;
aux[4] = (2*v[4] + v[5]) % 256;
aux[5] = (v[4] + v[5]) % 256;
aux[6] = (2*v[6] + v[7]) % 256;
aux[7] = (v[6] + v[7]) % 256;

@ A opera��o final $T$ � exatamente como a primeira opera��o, exceto que a
chave utilizada aqui deve ser a �ltima chave gerada (isto �, se temos |rounds|
rounds, a �ltima chave utilizada at� o momento foi a chave de n�mero
|2*rounds| e, portanto, para a �ltima opera��o, usaremos a chave de n�mero
|2*rounds + 1|).
@<Ope...@>=
v[0] = v[0] ^ k[2*rounds+1][0];
v[1] = v[1] + k[2*rounds+1][1];
v[2] = v[2] + k[2*rounds+1][2];
v[3] = v[3] ^ k[2*rounds+1][3];
v[4] = v[4] ^ k[2*rounds+1][4];
v[5] = v[5] + k[2*rounds+1][5];
v[6] = v[6] + k[2*rounds+1][6];
v[7] = v[7] ^ k[2*rounds+1][7];

@
@<Inversa da Opera��o T@>=
v[0] = v[0] ^ k[2*rounds+1][0];
v[1] = v[1] - k[2*rounds+1][1];
v[2] = v[2] - k[2*rounds+1][2];
v[3] = v[3] ^ k[2*rounds+1][3];
v[4] = v[4] ^ k[2*rounds+1][4];
v[5] = v[5] - k[2*rounds+1][5];
v[6] = v[6] - k[2*rounds+1][6];
v[7] = v[7] ^ k[2*rounds+1][7];

@
@<Inversa do Sex...@>=
aux[0] = v[0] - v[1];
aux[1] = 2*v[1] - v[0];
aux[2] = v[2] - v[3];
aux[3] = 2*v[3] - v[2];
aux[4] = v[4] - v[5]; 
aux[5] = 2*v[5] - v[4];
aux[6] = v[6] - v[7];
aux[7] = 2*v[7] - v[6];

@
@<Inversa do Qui...@>=
v[6] = aux[3] - aux[7];
v[7] = 2*aux[7] - aux[3];
v[4] = aux[2] - aux[6];
v[5] = 2*aux[6] - aux[2];
v[2] = aux[1] - aux[5];
v[3] = 2*aux[5] - aux[1];
v[0] = aux[0] - aux[4];
v[1] = 2*aux[4] - aux[0];

@
@<Inversa do Qua...@>=
aux[6] = v[3] - v[7];
aux[7] = 2*v[7] - v[3];
aux[4] = v[2] - v[6];
aux[5] = 2*v[6] - v[2];
aux[2] = v[1] - v[5];
aux[3] = 2*v[5] - v[1];
aux[0] = v[0] - v[4];
aux[1] = 2*v[4] - v[0];

@
@<Inversa do Ter...@>=
v[0] = aux[0] - k[2*j][0];
v[1] = aux[1] ^ k[2*j][1];
v[2] = aux[2] ^ k[2*j][2];
v[3] = aux[3] - k[2*j][3];
v[4] = aux[4] - k[2*j][4];
v[5] = aux[5] ^ k[2*j][5];
v[6] = aux[6] ^ k[2*j][6];
v[7] = aux[7] - k[2*j][7];

@
@<Inversa do Seg...@>=
v[0] = log[v[0]];
v[1] = exp[v[1]];
v[2] = exp[v[2]];
v[3] = log[v[3]];
v[4] = log[v[4]];
v[5] = exp[v[5]];
v[6] = exp[v[6]];
v[7] = log[v[7]];

@
@<Inversa do Pri...@>=
v[0] = v[0] ^ k[2*j-1][0];
v[1] = v[1] - k[2*j-1][1];
v[2] = v[2] - k[2*j-1][2];
v[3] = v[3] ^ k[2*j-1][3];
v[4] = v[4] ^ k[2*j-1][4];
v[5] = v[5] - k[2*j-1][5];
v[6] = v[6] - k[2*j-1][6];
v[7] = v[7] ^ k[2*j-1][7];

@
@<Fin...@>=
fclose(entrada);
fclose(saida);

@* Indice.



