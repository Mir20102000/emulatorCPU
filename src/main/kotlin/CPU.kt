// безадресные команды
// Гарвардская архитектура
// поиск максимума среди элементов массива

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

// Тип команды
enum class Instructions(val code: UInt) {
    PUSH(0b0001u),   // При выполнении команды PUSH, следующая команда кладется на вершину стека, т.е. она не команда, а число.
    READ(0b0010u),   // При выполнении команды в стек кладется число из памяти по адресу, лежащему на вершине стека. Т.е. если вы хотите прочитать число из 100 адреса, то вы должны выполнить следующие команды: PUSH 100 READ.
    WRITE(0b0011u),  // При выполнении команды, в адрес лежащий на вершине стека, кладется число, которое лежало на стеке под адресом. Оба значения со стека естественно исчезают.
    DUP(0b0100u),    // Продублировать число на вершине стека
    DROP(0b0101u),   // Удалить число с вершины стека
    JMP(0b0110u),    // Загрузить число со стека в регистр-счетчик
    OUT(0b0111u),    // Вывод из вершины стека
    CMP(0b1000u),    // Сравнивает два верхних числа на стеке
    INC(0b1001u),    // Инкремент числа на вершине стека
    DEC(0b1010u),    // декремент числа на вершине стека
    JE(0b1011u),     // прыжок с проверкой если равно
    JG(0b1100u),     // прыжок с проверкой если больше
    ADD(0b1101u),    // Складывает числа на вершине стека, результат кладется на вершину стека. Слагаемые исчезают со стека.
    JL(0b1110u),     // прыжок с проверкой если меньше
    HLT(0b1111u)     // Остановка выполнения программы
}


data class Command(
    val instructions: Instructions,
) {

    constructor(code: UInt) : this(
        instructions = Instructions.values().first { it.code == code.getBits(0, 3) }, // Получение типа команды из 32-битного кода
    )
}

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
            Instructions.PUSH -> {
                pc++ // Переход к следующему числу в памяти
                dataRAM[stackMemoryStart + sp++] = commandRAM[pc] // Помещаем число на вершину стека
                pc++ // Увеличиваем счетчик команд для перехода к следующей команде
            }

            Instructions.READ -> {
                val address = dataRAM[stackMemoryStart + --sp] // Получаем адрес из вершины стека
                dataRAM[stackMemoryStart + sp++] = dataRAM[address] // Читаем из памяти и кладем значение на вершину стека
                pc++
            }

            Instructions.WRITE -> {
                val address = dataRAM[stackMemoryStart + --sp] // Получаем адрес для записи
                dataRAM[address] = dataRAM[stackMemoryStart + --sp] // Записываем значение на указанное место в памяти
                pc++
            }

            Instructions.DUP -> {
                dataRAM[stackMemoryStart + sp] = dataRAM[stackMemoryStart + sp - 1u] // Дублируем значение на вершине стека
                sp++ // Увеличиваем указатель стека
                pc++
            }

            Instructions.DROP -> {
                sp-- // Убираем вершину стека
                pc++
            }

            Instructions.JMP -> {
                val point = dataRAM[stackMemoryStart + --sp]
                pc = point
            }

            Instructions.OUT -> {
                output.add(dataRAM[stackMemoryStart + --sp].toString())
                pc++
            }

            Instructions.CMP -> {
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

            Instructions.INC -> {
                dataRAM[stackMemoryStart + sp - 1u]++ // Инкрементируем значение на вершине стека
                pc++
            }

            Instructions.DEC -> {
                dataRAM[stackMemoryStart + sp - 1u]-- // Декрементируем значение на вершине стека
                pc++
            }

            Instructions.JE -> {
                val point = dataRAM[stackMemoryStart + --sp]
                if (flag.getBits(2, 2) == 0u && flag.getBits(1, 1) == 0u) { // например, если flag = 0b001 (числа равны), переход произойдет
                    pc = point    // проверяем число сравнений
                } else pc++
                //flag = 1u
            }

            Instructions.JG -> {
                val point = dataRAM[stackMemoryStart + --sp]
                if (flag.getBits(1, 1) == 1u) { // например, если flag = 0b010 (первое число больше второго)
                    pc = point // проверяем число сравнений
                } else pc++
                //flag = 1u
            }

            Instructions.ADD -> {
                val b = dataRAM[stackMemoryStart + --sp]
                val a = dataRAM[stackMemoryStart + --sp]
                dataRAM[stackMemoryStart + sp++] = a + b // Складываем два верхних значения на стеке
                pc++
            }

            Instructions.JL -> {
                val point = dataRAM[stackMemoryStart + --sp]
                if (flag.getBits(2, 2) == 1u) { // если flag = 0b100 (первое число меньше второго)
                    pc = point
                } else pc++
                //flag = 1u
            }


            Instructions.HLT -> {
                flag = 0u
            }

        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
data class ArrayItem(val name: String, val start: UInt, val elements: UIntArray)

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class, DelicateCoroutinesApi::class)
object CpuController {
    private lateinit var cpu: Cpu
    private var executionJob: Job? = null
    var executionDelay = 50L
        private set
    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState

    // алгоритм нахождения максимума среди элементов массива
    var program = uintArrayOf(
        0b0001u, 1u,  // 1. PUSH 89     Кладём в стек 89
        0b0010u,      // 2. READ        Читаем значение из адреса 89, текущий макс элемент (первый)
        0b0001u, 0u,  // 3. PUSH        Кладем в стек 88
        0b0010u,      // 4. READ        Читаем значение из адреса 88, размер массива
        0b0001u, 1u,  // 5. PUSH 89     Кладем в стек 89
        0b1101u,      // 6. ADD         Складываем, получаем адрес, в котором будет максимальный элемент (следующая ячейка после конца массива)
        0b0011u,      // 7. WRITE       Записываем в ячейку после массива первый элемент

        0b0001u, 0u, // 8. PUSH 88
        0b0010u,      // 9. READ        Читаем значение из адреса 88.

        0b1010u,      // 10. DEC        Уменьшаем вершину на 1 (13 команда-loop перед ней)
        0b0100u,      // 11. DUP        Дублируем вершину
        0b0001u, 0u,  // 12. PUSH 0     Кладем в стек 0
        0b1000u,      // 13. CMP        Cравниваем с нулем
        0b0001u, 54u, // 14. PUSH 54    Кладем в стек 54 для перехода к концу
        0b1011u,      // 15. JE         Если сравнения закончились, то переходим к выводу результата, иначе продолжаем
        0b0100u,      // 16. DUP        Дублируем вершину
        0b1001u,      // 17. INC        Увеличиваем на 1
        0b0001u, 0u, // 18. PUSH 88    Кладем в стек 88
        0b1101u,      // 19. ADD        Складываем вершины
        0b0010u,      // 20. READ       Читаем значение из адреса
        0b0001u, 0u, // 21. PUSH 88    Кладем в стек 88
        0b0010u,      // 22. READ       Читаем значение из адреса, размер массива
        0b0001u, 1u, // 23. PUSH 89    Кладем в стек 89
        0b1101u,      // 24. ADD        Складываем, получаем адрес, в котором будет максимальный элемент (следующая ячейка после конца массива)
        0b0010u,      // 25. READ       Читаем значение из адреса
        0b1000u,      // 26. CMP        Сравниваем две вершины
        0b0001u, 13u, // 27. PUSH 13    Кладем в стек 13 для гипотетического перехода
        0b1100u,      // 28. JG         Если максимум больше, заново идем в цикл, иначе обновляем максимум

        0b0100u,      // 29. DUP        Дублируем вершину
        0b1001u,      // 30. INC        Увеличиваем на 1
        0b0001u, 0u, // 31. PUSH 88    Кладем в стек 88
        0b1101u,      // 32. ADD        Складываем вершины
        0b0010u,      // 33. READ       Читаем значение из адреса
        0b0001u, 0u, // 34. PUSH 88    Кладем в стек 88
        0b0010u,      // 35. READ       Читаем значение из адреса, размер массива
        0b0001u, 1u, // 36. PUSH 89    Кладем в стек 89
        0b1101u,      // 37. ADD        Складываем, получаем адрес, в котором будет максимальный элемент (следующая ячейка после конца массива)
        0b0011u,      // 38. WRITE      Записываем текущее наибольшее значение
        0b0001u, 13u, // 39. PUSH 13    Кладем в стек 13 для перехода
        0b0110u,      // 40. JMP        Заново идем в цикл

        0b0001u, 0u, // 41. PUSH 88    Команда номер 54 финальный вывод (END перед ней) // кладем в стек 88
        0b0010u,      // 42. READ       Читаем значение из адреса, размер массива
        0b0001u, 1u, // 43. PUSH 89    Сладем в стек 89
        0b1101u,      // 44. ADD        Складываем, получаем адрес, в котором будет максимальный элемент (следующая ячейка после конца массива)
        0b0010u,      // 45. READ       Читаем значение из адреса
        0b0111u,      // 46. OUT        Выводим результат
        0b1111u       // 47. HLT        Остановка выполнения программы
    )

    var array = listOf(ArrayItem("data", 0u, uintArrayOf(
        10u, 77u, 15u, 3u, 18u, 7u, 1u, 111u, 53u, 11u, 21u
    )))

    init {
        reset()
    }

    fun resetFile(filePath: String) {
        // Проверка на существование файла
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("Файл не найден: $filePath")
        }
        // Считываем все строки, убираем пустые строки и комментарии "//"
        val lines = File(filePath).readLines().filter { it.isNotBlank() && !it.startsWith("//") }

        val program = mutableListOf<UInt>() // команды программы нахождения максимума в массиве
        val data = mutableListOf<ArrayItem>() // массив значений
        val labels = mutableMapOf<String, Int>() // метки с их адресами для переходов

        // первый цикл для меток и массивов, обрабатываем каждую строку
        var address = 0
        for (line in lines) {
            val parts = line.split(" ") // делим строку на части
            // если строка состоит из более двух частей и, если первая часть строки совпадает с одним из инструкций из Instructions
            if (parts.size > 1 && !Instructions.values().map { it.name }.contains(parts[0])) {
                // если вторая часть строки это числа, разделённые ","
                if(parts[1].split(",").size > 1 && !parts[1].startsWith(",") && !parts[1].endsWith(",")) {
                    val elements = parts[1].trim().split(",").map { it.trim().toUInt() } // присваиваем числа массиву elements
                    // сохраняем в массив data числа
                    data.add(
                        ArrayItem(
                            parts[0],
                            0u,
                            elements.toUIntArray()
                        )
                    )
                }
            }  else { // если строка заканчивается ":", то это label
                if (line.endsWith(":")) {
                    val label = line.removeSuffix(":")
                    labels[label] = address
                }
                val parts = line.split(" ")
                if(parts.size > 1){
                    if(parts[0] == "JL" || parts[0] == "JG" || parts[0] == "JE" || parts[0] == "JE") {
                        address++
                    }
                    address++
                }
                address++
            }
        }

        // работаем с инструкциями
        for (line in lines) {
            val parts = line.split(" ")
            if (Instructions.values().any { it.name == parts[0] }) {
                val instruction = Instructions.valueOf(parts[0])
                when (instruction) {
                    Instructions.PUSH -> {
                        if (parts.size > 1) {
                            program.add(instruction.code)
                            val parts2 = parts[1].split("+")
                            if(data.any{it.name == parts2[0]}){
                                if(parts2.size > 1){
                                    if(parts2[1].toIntOrNull() != null){
                                        program.add(data.find{it.name==parts2[0]}!!.start + parts2[1].toUInt())
                                    } else error("Undefined increase: ${parts[1]}")
                                } else program.add(0u)
                            } else program.add(parts[1].toUInt())
                        }
                    }

                    Instructions.JL, Instructions.JG, Instructions.JE, Instructions.JMP -> {
                        if (parts.size > 1) {
                            if (labels[parts[1]]?.toUInt() != null) {
                                program.add(Instructions.PUSH.code)
                                program.add(labels[parts[1]]?.toUInt()!!)
                                program.add(instruction.code)
                            } else {
                                error("Undefined label: ${parts[1]}")
                            } }
                    }
                    else -> program.add(instruction.code)
                }
            }
        }

        reset(program.toUIntArray(), data)
    }


    fun reset(program: UIntArray = uintArrayOf(), array: List<ArrayItem> = listOf()) {
        cpu = Cpu()
        if (program.isEmpty() and array.isEmpty()) {
            for(i in this.array){
                cpu.loadArray(i)
            }
            cpu.loadProgram(this.program)
        } else {
            for(i in array){
                cpu.loadArray(i)
            }
            cpu.loadProgram(program)
        }
        updateState()
        pause()
    }

    private fun updateState() {

        val pcRegister = Register("PC", cpu.pc.toString(16).uppercase())
        val spRegister = Register("SP", cpu.sp.toString(16).uppercase())
        val flag = Register("FLAG", value = cpu.flag.toString(2).padStart(3, '0'))

        val stackList = cpu.dataRAM.slice(cpu.stackMemoryStart.toInt()..cpu.stackMemoryEnd.toInt()).take(cpu.sp.toInt())
            .mapIndexed { index, value ->
                Register("S$index", value.toHexString())
            }

        val dataMemoryList = cpu.dataRAM.mapIndexed { index, value ->
            MemoryCell(index.toString(), value.toHexString())
        }

        val commands = cpu.commandRAM
        val commandsList = commands.takeWhile { it != 0b1111u }
            .plus(commands.find { it == 0b1111u })
            .filterNotNull()
            .mapIndexed { index, value ->
                CommandState(
                    index = index,
                    name = if (index > 0 && commands[index - 1] == 0b0001u)
                        value.toInt().toString()
                    else
                        Instructions.values().find { it.code == value }?.name ?: value.toInt().toString(),
                    hexValue = value.toHexString(),
                    isCurrent = index == cpu.pc.toInt()
                )
            }

        _screenState.value = ScreenState(
            generalRegisters = listOf(pcRegister, spRegister, flag),
            dataRAM = dataMemoryList,
            stack = stackList,
            commands = commandsList,
            output = cpu.output,
        )
    }

    fun resume() {
        if (_screenState.value.isHalted && executionJob == null && cpu.flag.getBits(0, 0) == 1u) {
            _screenState.value = _screenState.value.copy(isHalted = false)
            executionJob = GlobalScope.launch {
                while (!_screenState.value.isHalted) {
                    delay(executionDelay)
                    cpu.executeCommand()
                    updateState()
                }
            }
        }
    }

    fun pause() {
        _screenState.value = _screenState.value.copy(isHalted = true)
        executionJob?.cancel()
        executionJob = null
    }

    fun next() {
        if (_screenState.value.isHalted && cpu.flag.getBits(0, 0) == 1u) {
            cpu.executeCommand()
            updateState()
            _screenState.value = _screenState.value.copy(isHalted = true)
        }
    }
}
