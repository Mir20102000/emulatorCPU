package cpu.model

data class Command(
    val instructions: Instruction,
) {

    constructor(code: UInt) : this(
        instructions = Instruction.values().first { it.code == code },
    )
}
