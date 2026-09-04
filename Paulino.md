Modulo Pedagogico de acordo o sistema de ensino angolano, entidades e os relacionamentos nesse modulo.  

E com a capacidades de gera Boletim e Mini-Pauta, mas o agora utiliza-se o modelo de MAC-NPT-MT e não MAC-NPP-NPT-MT.

Como eu devo estruturar as entidades.

Sim. E, com a alteração para MAC + NPT → MT, eu recomendo reformular o módulo pedagógico que estávamos a montar anteriormente.

Há uma confirmação importante: o Decreto Executivo n.º 424/25, de 18 de junho, estabelece para classes de transição a fórmula:

MT = (MAC + NPT) / 2, onde MAC é a média das avaliações contínuas do trimestre e NPT é a nota da prova trimestral.

Isso significa que não devemos modelar MAC, NPT e MT como três avaliações independentes. A MAC é calculada a partir das avaliações contínuas; a NPT é uma avaliação trimestral específica; e a MT é um resultado calculado.

1. Arquitetura que recomendo

Para o teu módulo pedagógico, eu estruturaria assim:

                    SchoolClass
                         │
                         │
                    Enrolment
                         │
                         ▼
                      Student
                         │
                         │
                         ▼
                 ┌───────────────┐
                 │               │
                 ▼               ▼
             Discipline      Evaluation
                 │               │
                 │               │
                 └───────┬───────┘
                         │
                         ▼
                       Grade
                         │
                         ▼
                  TrimesterResult

Mas há uma observação importante:

Student, Enrolment e SchoolClass não precisam ser entidades do módulo pedagógico.

Elas já pertencem ao módulo da Secretaria.

Portanto, o módulo pedagógico deve reutilizar os relacionamentos existentes.

2. As 5 entidades principais

Como anteriormente você queria trabalhar com apenas cinco entidades, eu manteria:

1. Discipline
2. Schedule
3. Evaluation
4. Grade
5. TrimesterResult

Essa estrutura é suficiente para:

lançar avaliações;
calcular MAC;
lançar NPT;
calcular MT;
calcular média final;
determinar situação;
gerar boletim;
gerar mini-pauta.
3. Discipline

Representa a disciplina.

Exemplo:

Matemática
Língua Portuguesa
História
Geografia
Educação Física

Relacionamento:

Discipline
     │
     ├──────────< Evaluation
     │
     └──────────< Schedule

Uma disciplina pode possuir várias avaliações.

4. Schedule

Representa o horário da disciplina.

Schedule
   │
   └──────> Discipline

Exemplo:

Segunda-feira
08:00 - 08:45
Matemática
7ª Classe
Turma A

Eu acrescentaria posteriormente Teacher e SchoolClass ao horário.

Schedule
   │
   ├── Discipline
   ├── Teacher
   └── SchoolClass
5. Evaluation

Aqui está uma das principais mudanças.

Não devemos criar:

MAC
NPT
MT

como entidades.

Devemos ter Evaluation para representar aquilo que realmente é uma avaliação.

Por exemplo:

Evaluation

1º Trimestre
   │
   ├── Trabalho de Matemática
   ├── Exercício de Matemática
   ├── Avaliação Oral
   ├── Teste
   └── Prova Trimestral

Então precisamos de um enum:

public enum EvaluationType {

    CONTINUA("Avaliação Contínua"),

    PROVA_TRIMESTRAL("Prova Trimestral");

    private final String descricao;

    EvaluationType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
6. Como a Evaluation funciona

Imagine:

Matemática — I Trimestre

O professor lança:

Avaliação 1 = 8
Avaliação 2 = 10
Avaliação 3 = 7
Avaliação 4 = 9

Todas são:

CONTINUA

Depois:

NPT = 8

que é:

PROVA_TRIMESTRAL

Então:

MAC = (8 + 10 + 7 + 9) / 4

MAC = 8,5

E:

MT = (MAC + NPT) / 2

MT = (8,5 + 8) / 2

MT = 8,25

Portanto:

MAC = 8,5
NPT = 8
MT  = 8,25

O MT não é lançado pelo professor.

O sistema calcula.

7. Grade

A Grade representa a nota que determinado aluno obteve numa determinada avaliação.

O relacionamento fica:

Evaluation
     │
     │ 1
     │
     │
     │ N
     ▼
   Grade
     ▲
     │
     │
     │
  Enrolment

Ou seja:

Evaluation 1 ────────< Grade >──────── Enrolment

Exemplo:

Evaluation:
Matemática
I Trimestre
Avaliação Contínua 1

E:

Grade:

Aluno: João
Nota: 8

Outro aluno:

Grade:

Aluno: Maria
Nota: 10
8. Então onde fica a MAC?

Não fica armazenada em cada Grade.

Ela é calculada.

Por exemplo:

Grade
 ├── Avaliação contínua 1 = 8
 ├── Avaliação contínua 2 = 10
 ├── Avaliação contínua 3 = 7
 └── Avaliação contínua 4 = 9

O Service faz:

MAC = média das Grades
9. E onde fica o NPT?

Também é obtido através da Grade.

Exemplo:

Evaluation
    │
    ├── type = CONTINUA
    │
    └── trimester = 1

e:

Evaluation
    │
    ├── type = PROVA_TRIMESTRAL
    │
    └── trimester = 1

Assim o sistema consegue separar:

CONTINUA

de:

PROVA_TRIMESTRAL
10. TrimesterResult

Aqui entra a quinta entidade.

Essa entidade é extremamente importante para o teu sistema.

Ela representa o resultado consolidado de um aluno numa disciplina em determinado trimestre.

Exemplo:

TrimesterResult

Aluno:
João

Disciplina:
Matemática

Trimestre:
1

MAC:
8,5

NPT:
8

MT:
8,25

Situação:
APROVADO

Relacionamento:

Enrolment
    │
    │
    ▼
TrimesterResult
    ▲
    │
    │
Discipline
11. Estrutura completa

Eu visualizaria o banco assim:

                         SECRETARIA
                              │
                              │
                         Enrolment
                              │
                              │
             ┌────────────────┴────────────────┐
             │                                 │
             ▼                                 ▼
          Student                         SchoolClass
                                               
                                               
                         PEDAGÓGICO
                              │
                              ▼
                         Discipline
                         /        \
                        /          \
                       ▼            ▼
                 Evaluation      Schedule
                      │
                      │
                      ▼
                    Grade
                      │
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
      Enrolment              Evaluation
          │
          ▼
   TrimesterResult
12. O ciclo completo de avaliação

O funcionamento será:

Passo 1 — Criar disciplina
Matemática
Passo 2 — Criar avaliações
I Trimestre

Avaliação Contínua 1
Avaliação Contínua 2
Avaliação Contínua 3
Prova Trimestral
Passo 3 — Lançar notas
João

AC1 = 8
AC2 = 10
AC3 = 7
NPT = 8
Passo 4 — Sistema calcula MAC
MAC = (8 + 10 + 7) / 3

MAC = 8,33
Passo 5 — Sistema calcula MT
MT = (8,33 + 8) / 2

MT = 8,165

Arredondamento/apresentação deve seguir a regra definida para a etapa de ensino. O regulamento de 2025 prevê arredondamento das classificações numéricas trimestrais e finais à unidade mais próxima no Ensino Primário e Secundário.

13. E o MFD?

Depois dos três trimestres:

MT1
MT2
MT3

calculamos:

MFD = (MT1 + MT2 + MT3) / 3

Portanto:

Matemática

MT1 = 8
MT2 = 9
MT3 = 10

MFD = 9

O Decreto Executivo n.º 424/25 também trata a média final por disciplina a partir das médias trimestrais para situações sem exames combinados.

14. Precisamos de uma sexta entidade para MFD?

Não necessariamente.

Podemos ter:

TrimesterResult

para:

MT1
MT2
MT3

e calcular:

MFD

no Service.

Por exemplo:

public BigDecimal calcularMediaFinal(
        BigDecimal mt1,
        BigDecimal mt2,
        BigDecimal mt3) {

    return mt1
            .add(mt2)
            .add(mt3)
            .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
}
15. Como gerar o Boletim

O boletim não precisa ser uma entidade.

É um documento gerado pelo sistema.

O Service consulta:

Aluno
 ↓
Matrícula
 ↓
Disciplinas
 ↓
Resultados trimestrais
 ↓
MFD
 ↓
Situação

E produz:

                    BOLETIM DE NOTAS

Aluno: João Manuel
Classe: 7ª
Turma: A
Ano Lectivo: 2026

┌──────────────────┬─────┬─────┬─────┬─────┐
│ DISCIPLINA       │ MT1 │ MT2 │ MT3 │ MFD │
├──────────────────┼─────┼─────┼─────┼─────┤
│ Matemática       │  8  │  9  │ 10  │  9  │
│ Português        │  7  │  8  │  9   │ 8  │
│ História         │  9  │  8  │  8   │ 8  │
└──────────────────┴─────┴─────┴─────┴─────┘

E, se quiseres mostrar o detalhamento:

                 I TRIMESTRE

Disciplina       MAC    NPT    MT

Matemática       8,5     8    8,25
Português        7,0     8    7,50
História         9,0     8    8,50
16. Como gerar a Mini-Pauta

A Mini-Pauta é diferente.

Ela normalmente trabalha com todos os alunos da turma.

Exemplo:

MINI-PAUTA
7ª CLASSE
TURMA A
I TRIMESTRE

Aluno       MAT                  PORTUGUÊS
            MAC NPT MT           MAC NPT MT

João        8   8   8            9   8   9
Maria       7   9   8            8   8   8
Pedro       6   7   7            7   8   8

Portanto, a Mini-Pauta pode ser gerada através de uma consulta:

SchoolClass
      │
      ▼
Enrolments
      │
      ▼
Students
      │
      ▼
TrimesterResults
      │
      ▼
Discipline
17. Uma decisão importante para o teu sistema

Eu não recomendo colocar:

private Double mac;
private Double npt;
private Double mt;

dentro de Grade.

Isso criaria redundância.

A Grade deve representar somente:

"Qual nota este aluno obteve nesta avaliação?"

Exemplo:

Grade

Aluno: João
Avaliação: Teste de Matemática
Nota: 8

Já o TrimesterResult representa:

"Qual foi o resultado deste aluno na disciplina neste trimestre?"

TrimesterResult

Aluno: João
Disciplina: Matemática
Trimestre: 1

MAC: 8,33
NPT: 8
MT: 8,17

Essa separação deixa o modelo muito mais correto.

18. As cinco entidades finais

Portanto, eu fecharia o módulo pedagógico assim:

Entidade	Responsabilidade
Discipline	Cadastro das disciplinas
Schedule	Horário das disciplinas
Evaluation	Define cada avaliação e o seu tipo
Grade	Nota do aluno em cada avaliação
TrimesterResult	MAC, NPT, MT e situação do trimestre

E os relacionamentos principais:

Discipline 1 ─────── N Evaluation

Discipline 1 ─────── N Schedule

Evaluation 1 ─────── N Grade

Enrolment 1 ──────── N Grade

Enrolment 1 ──────── N TrimesterResult

Discipline 1 ─────── N TrimesterResult
O ponto mais importante

O teu modelo passa a refletir exatamente o processo:

AVALIAÇÕES CONTÍNUAS
        │
        ▼
       MAC
        │
        ├────────── NPT
        │             │
        └──────┬──────┘
               ▼
              MT
               │
          MT1 MT2 MT3
               │
               ▼
              MFD
               │
               ▼
        APROVADO/REPROVADO

E isso permite gerar tanto o Boletim quanto a Mini-Pauta sem criar entidades artificiais apenas para representar documentos.

Nota: para o teu sistema de Pré até à 9.ª classe, eu deixaria a regra de cálculo parametrizável por classe/ano letivo, porque há diferenças entre etapas e as normas podem mudar. Além disso, o regulamento de 2025 distingue, por exemplo, classes de transição e classes de exame nacional (incluindo 6.ª e 9.ª).