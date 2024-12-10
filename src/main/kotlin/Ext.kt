// извлечение битов, нужен для работы с флагом CMR
fun UInt.getBits(start: Int, end: Int): UInt {
    return this shl (31 - end) shr (start + 31 - end)
}

// следующие методы для определения get и set для массивов с типом данных Uint (32 битное целое число)
operator fun <T> Array<T>.get(index: UInt) = this[index.toInt()]

operator fun <T> Array<T>.set(index: UInt, value: T) = this.set(index.toInt(), value)

@OptIn(ExperimentalUnsignedTypes::class)
operator fun UIntArray.get(index: UInt) = this[index.toInt()]

@OptIn(ExperimentalUnsignedTypes::class)
operator fun UIntArray.set(index: UInt, value: UInt) = this.set(index.toInt(), value)