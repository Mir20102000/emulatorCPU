import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import controller.CpuController


@OptIn(ExperimentalUnsignedTypes::class)
@Composable
@Preview
fun App() {


    val state by CpuController.screenState.collectAsState()

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {

            Row {

                Column(modifier = Modifier.padding(top = 30.dp)) {
                    Box(
                        modifier = Modifier
                            .border(2.dp, Color.Black) // Рамка для всей таблицы
                            .padding(8.dp) // Отступы внутри рамки для таблицы
                    ) {
                        Column {
                            state.generalRegisters.forEach { register ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp) // Вертикальные отступы между строками
                                ) {
                                    Text(register.name, modifier = Modifier.width(60.dp))
                                    Text(
                                        if (register.name != "FLAG") register.value.toInt(radix = 16)
                                            .toString() else register.value
                                    )
                                }
                            }
                        }
                    }
                }


                // Отображение команд
                val listState = rememberLazyListState() // Состояние списка

                val currentIndex = state.commands.indexOfFirst { it.isCurrent }

                // Автоматическая прокрутка к текущей команде
                LaunchedEffect(currentIndex) {
                    if (currentIndex >= 0) {
                        listState.animateScrollToItem(currentIndex)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .padding(start = 40.dp)
                        .width(200.dp),
                    state = listState // Передаём состояние списка
                ) {
                    item {
                        Text(text = "Command memory:", fontWeight = FontWeight.Bold)
                    }
                    items(state.commands) { command ->
                        val modifier = if (command.isCurrent) {
                            Modifier.background(color = Color(0xffff9800))
                        } else {
                            Modifier
                        }
                        Row(modifier = modifier) {
                            Text(command.index.toString(), modifier = Modifier.width(40.dp), color = Color.Gray)
                            Text(command.hexValue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(command.name)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(start = 40.dp)
                        .width(160.dp)
                ) {
                    item {
                        Text(text = "Data memory:", fontWeight = FontWeight.Bold)
                    }
                    items(state.dataRAM) { memoryCell ->
                        Row {
                            Text(memoryCell.address, modifier = Modifier.width(40.dp), color = Color.Gray)
                            Text(memoryCell.hexValue)
                        }
                    }
                }

                // Отображение состояния стека
                Column(modifier = Modifier.padding(start = 20.dp).width(100.dp)) {
                    Text(text = "Stack:", fontWeight = FontWeight.Bold)
                    state.stack.reversed().forEach { stackItem ->
                        Text(stackItem.value) // Показываем значение на вершине стека
                    }
                }

                Column(modifier = Modifier.padding(start = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {


                    Text(text = "Result:", fontWeight = FontWeight.Bold)
                    state.output.forEach { outputLine ->
                        Text(outputLine)
                    }

                }

                Column(modifier = Modifier.padding(start = 100.dp, top = 200.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    Button(
                        modifier = Modifier.width(300.dp).height(33.dp),
                        onClick = { CpuController.pause() },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xffffffff),
                            backgroundColor = Color(0xff0d47a1)
                        )
                    ) { Text("Stop", textAlign = TextAlign.Center) } // остановить

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.width(300.dp).height(33.dp),
                        onClick = { CpuController.resume() },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xffffffff),
                            backgroundColor = Color(0xff0d47a1)
                        )
                    ) { Text("Start/Resume", textAlign = TextAlign.Center) } // начать/продолжить


                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.width(300.dp).height(33.dp),
                        onClick = { CpuController.next() },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xffffffff),
                            backgroundColor = Color(0xff0d47a1)
                        )
                    ) { Text("Next step", textAlign = TextAlign.Center) }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.width(300.dp).height(33.dp),
                        onClick = {
                            CpuController.resetFile("C:\\Users\\ASUS\\Desktop\\RazrabotkaCPU\\src\\main\\resources\\program.txt")
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xffffffff),
                            backgroundColor = Color(0xff0d47a1)
                        )
                    ) { Text("Read file", textAlign = TextAlign.Center) } // Чтение файла

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.width(300.dp).height(33.dp),
                        onClick = {
                            CpuController.reset()
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xffffffff),
                            backgroundColor = Color(0xff0d47a1)
                        )
                    ) { Text("Reset", textAlign = TextAlign.Center) } // сброс выполнения

                }

                Spacer(modifier = Modifier.height(64.dp))
            }

        }
    }
}

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(1000.dp, 800.dp))
    Window(onCloseRequest = ::exitApplication, state = windowState) {
        App()
    }
}