
# Microjava-compiler
Example micro java source code: [input.mj](https://github.com/nikola00nikola/Microjava-compiler/blob/main/Microjava-Compiler/test/program.mj)

Simple and functional compiler for micro java.
The compiler works in **4 phases**.
- The first phase is **Lexical Analysis**, which parses the [input.mj](https://github.com/nikola00nikola/Microjava-compiler/blob/main/Microjava-Compiler/test/program.mj) source file and outputs lexical tokens used in the [input.mj](https://github.com/nikola00nikola/Microjava-compiler/blob/main/Microjava-Compiler/test/program.mj) file. Lexical tokens are given in [mjlexer.flex](https://github.com/nikola00nikola/Microjava-compiler/blob/main/Microjava-Compiler/spec/mjlexer.flex) file. Lexical analysis uses JFlex library. 
- The second phase is **Syntax Analysis**, which performs syntax analysis based on the grammar provided in the [mjparser.cup](https://github.com/nikola00nikola/Microjava-compiler/blob/main/Microjava-Compiler/spec/mjparser.cup) file. It takes lexical tokens obtained in the first phase as input and produces an object corresponding to the Program's parse tree from the .cup file as output. Syntax analysis uses cup library, which is implemented with LALR parser generator. 
- The third phase is **Semantic Analysis**, which is responsible for initializing the symbol table based on the Program object. The symbol table is implemented using the school library SymbolTable.
- The fourth phase is **Code Generation**. The implementation of the fourth phase is provided in the [CodeGenerator.java](https://github.com/nikola00nikola/Microjava-compiler/blob/main/Microjava-Compiler/src/rs/ac/bg/etf/pp1/CodeGenerator.java) file. Code generator uses utility class Code.


## Example

- Example microjava source code:
```micro java
program Example

	char msg[];
{
	void main()
		int i;
	{
		msg = new char[3];
		msg[0]='H';
		msg[1]='i';
		msg[2]='!';
		
		for(i=0; i<3; i++)
			print(msg[i]);
	}

}
```
- Generated microjava bytecode:
```micro java
codeSize=63
dataSize=1
mainPC=1
0: return
1: enter 0 1
4: const_3
5: newarray 0
7: putstatic 0
10: getstatic 0
13: const_0
14: const 72
19: bastore
20: getstatic 0
23: const_1
24: const 105
29: bastore
30: getstatic 0
33: const_2
34: const 33
39: bastore
40: const_0
41: store_0
42: load_0
43: const_3
44: jge 17 (=61)
47: getstatic 0
50: load_0
51: baload
52: const_1
53: bprint
54: load_0
55: const_1
56: add
57: store_0
58: jmp -16 (=42)
61: exit
62: return
```
