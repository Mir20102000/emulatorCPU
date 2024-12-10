package cpu
// безадресные команды, гарвардская архитектура, поиск максимума среди элементов массива

import cpu.model.Command
import cpu.model.Instruction
import getBits
import get
import set

@OptIn(ExperimentalUnsignedTypes::class)
class Cpu {

    var pc = 0u // счётчик команд - содержит адрес памяти, из которого в данный момент выполняется команда.
    var sp = 0u // указатель стека - хранит текущую верхнюю позицию стека
    var cmp = 0b001u // хранит результат сравнения двух чисел. Биты: 0 - равно, 1 - первое число больше, 2 - первое число меньше

    var commandMemory = UIntArray(1024) // память команд
    var dataMemory = UIntArray(1024) // память данных

    val stackStart = 500u
    val stackEnd = 515u
    val output = mutableListOf<String>() // переменная, которая хранит число для вывода


    fun loadProgram(program: UIntArray) {

        program.forEachIndexed { index, command -> // записываем в память команд саму программу вычисления максимума
            commandMemory[index] = command
        }
    }

    fun loadArray(array: ArrayItem) {
        array.elements.forEachIndexed { index, element ->
            dataMemory[index] = element
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun executeCommand() {
        val command = Command(commandMemory[pc])
        when (command.instructions) {
            Instruction.PUSH -> {
                pc++ // Переход к следующему числу в памяти
                dataMemory[stackStart + sp++] = commandMemory[pc] // Помещаем число на вершину стека
                pc++ // Увеличиваем счетчик команд для перехода к следующей команде
            }

            Instruction.READ -> {
                val address = dataMemory[stackStart + --sp] // Получаем адрес из вершины стека
                dataMemory[stackStart + sp++] = dataMemory[address] // Читаем из памяти и кладем значение на вершину стека
                pc++
            }

            Instruction.WRITE -> {
                val address = dataMemory[stackStart + --sp] // Получаем адрес для записи
                dataMemory[address] = dataMemory[stackStart + --sp] // Записываем значение на указанное место в памяти
                pc++
            }

            Instruction.DUP -> {
                dataMemory[stackStart + sp] = dataMemory[stackStart + sp - 1u] // Дублируем значение на вершине стека
                sp++ // Увеличиваем указатель стека
                pc++
            }

            Instruction.DROP -> {
                sp-- // Убираем вершину стека
                pc++
            }

            Instruction.JMP -> {
                val point = dataMemory[stackStart + --sp]
                pc = point
            }

            Instruction.OUT -> {
                output.add(dataMemory[stackStart + --sp].toString())
                pc++
            }

            Instruction.CMP -> {
                cmp = 1u
                val b = dataMemory[stackStart + --sp]
                val a = dataMemory[stackStart + --sp]
                if (b > a) {
                    //println("больше первое")
                    cmp = cmp or 0b010u
                } else if (a > b) {
                    //println("больше второе")
                    cmp = cmp or 0b100u
                }
                pc++
            }

            Instruction.INC -> {
                dataMemory[stackStart + sp - 1u]++ // Инкрементируем значение на вершине стека
                pc++
            }

            Instruction.DEC -> {
                dataMemory[stackStart + sp - 1u]-- // Декрементируем значение на вершине стека
                pc++
            }

            Instruction.JE -> {
                val point = dataMemory[stackStart + --sp]
                if (cmp.getBits(2, 2) == 0u && cmp.getBits(1, 1) == 0u) { // например, если flag = 0b001 (числа равны), переход произойдет
                    pc = point    // проверяем число сравнений
                } else pc++
                //flag = 1u
            }

            Instruction.JG -> {
                val point = dataMemory[stackStart + --sp]
                if (cmp.getBits(1, 1) == 1u) { // например, если flag = 0b010 (первое число больше второго)
                    pc = point // проверяем число сравнений
                } else pc++
                //flag = 1u
            }

            Instruction.ADD -> {
                val b = dataMemory[stackStart + --sp]
                val a = dataMemory[stackStart + --sp]
                dataMemory[stackStart + sp++] = a + b // Складываем два верхних значения на стеке
                pc++
            }

            Instruction.JL -> {
                val point = dataMemory[stackStart + --sp]
                if (cmp.getBits(2, 2) == 1u) { // если flag = 0b100 (первое число меньше второго)
                    pc = point
                } else pc++
                //flag = 1u
            }


            Instruction.HLT -> {
                cmp = 0u
            }

        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
data class ArrayItem(val name: String, val start: UInt, val elements: UIntArray)
