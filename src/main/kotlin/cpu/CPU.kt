package cpu
// безадресные команды, гарвардская архитектура, поиск максимума среди элементов массива

import controller.CommandState
import controller.MemoryCell
import controller.Register
import controller.ScreenState
import cpu.model.Command
import cpu.model.Instruction
import getBits
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import get
import set

@OptIn(ExperimentalUnsignedTypes::class)
class Cpu {

    val output = mutableListOf<String>()
    var pc = 0u // счётчик команд - содержит адрес памяти, из которого в данный момент выполняется команда.
    var sp = 0u // указатель стека - хранит текущую верхнюю позицию стека


//    var memory = UIntArray(1024) // единая память для команд и данных

//    var stack = UIntArray(8)
    var commandRAM = UIntArray(1024) // commandRAM - память команд, в которую будут загружены программы.
    var dataRAM = UIntArray(1024) // dataRAM - память данных, используется для хранения данных программы

    val stackMemoryStart = 30u
//    val stackMemoryEnd = 87u
    val stackMemoryEnd = 45u
    var flag = 0b001u // флаг для хранения результата сравнения двух чисел


    fun loadProgram(program: UIntArray) {

        program.forEachIndexed { index, command -> // записываем в память команд саму программу вычисления максимума
            commandRAM[index] = command
        }
    }

    fun loadArray(array: ArrayItem) {
        array.elements.forEachIndexed { index, element ->
            dataRAM[index] = element
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun executeCommand() {
        val command = Command(commandRAM[pc])
        when (command.instructions) {
            Instruction.PUSH -> {
                pc++ // Переход к следующему числу в памяти
                dataRAM[stackMemoryStart + sp++] = commandRAM[pc] // Помещаем число на вершину стека
                pc++ // Увеличиваем счетчик команд для перехода к следующей команде
            }

            Instruction.READ -> {
                val address = dataRAM[stackMemoryStart + --sp] // Получаем адрес из вершины стека
                dataRAM[stackMemoryStart + sp++] = dataRAM[address] // Читаем из памяти и кладем значение на вершину стека
                pc++
            }

            Instruction.WRITE -> {
                val address = dataRAM[stackMemoryStart + --sp] // Получаем адрес для записи
                dataRAM[address] = dataRAM[stackMemoryStart + --sp] // Записываем значение на указанное место в памяти
                pc++
            }

            Instruction.DUP -> {
                dataRAM[stackMemoryStart + sp] = dataRAM[stackMemoryStart + sp - 1u] // Дублируем значение на вершине стека
                sp++ // Увеличиваем указатель стека
                pc++
            }

            Instruction.DROP -> {
                sp-- // Убираем вершину стека
                pc++
            }

            Instruction.JMP -> {
                val point = dataRAM[stackMemoryStart + --sp]
                pc = point
            }

            Instruction.OUT -> {
                output.add(dataRAM[stackMemoryStart + --sp].toString())
                pc++
            }

            Instruction.CMP -> {
                flag = 1u
                val b = dataRAM[stackMemoryStart + --sp]
                val a = dataRAM[stackMemoryStart + --sp]
                if (b > a) {
                    //println("больше первое")
                    flag = flag or 0b010u
                } else if (a > b) {
                    //println("больше второе")
                    flag = flag or 0b100u
                }
                pc++
            }

            Instruction.INC -> {
                dataRAM[stackMemoryStart + sp - 1u]++ // Инкрементируем значение на вершине стека
                pc++
            }

            Instruction.DEC -> {
                dataRAM[stackMemoryStart + sp - 1u]-- // Декрементируем значение на вершине стека
                pc++
            }

            Instruction.JE -> {
                val point = dataRAM[stackMemoryStart + --sp]
                if (flag.getBits(2, 2) == 0u && flag.getBits(1, 1) == 0u) { // например, если flag = 0b001 (числа равны), переход произойдет
                    pc = point    // проверяем число сравнений
                } else pc++
                //flag = 1u
            }

            Instruction.JG -> {
                val point = dataRAM[stackMemoryStart + --sp]
                if (flag.getBits(1, 1) == 1u) { // например, если flag = 0b010 (первое число больше второго)
                    pc = point // проверяем число сравнений
                } else pc++
                //flag = 1u
            }

            Instruction.ADD -> {
                val b = dataRAM[stackMemoryStart + --sp]
                val a = dataRAM[stackMemoryStart + --sp]
                dataRAM[stackMemoryStart + sp++] = a + b // Складываем два верхних значения на стеке
                pc++
            }

            Instruction.JL -> {
                val point = dataRAM[stackMemoryStart + --sp]
                if (flag.getBits(2, 2) == 1u) { // если flag = 0b100 (первое число меньше второго)
                    pc = point
                } else pc++
                //flag = 1u
            }


            Instruction.HLT -> {
                flag = 0u
            }

        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
data class ArrayItem(val name: String, val start: UInt, val elements: UIntArray)
