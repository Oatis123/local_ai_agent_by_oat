import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Запускает бэкенд API в фоновом режиме, предполагая, что Python-скрипт сам запускает сервер.
 *
 * @param pythonExecutable Путь к исполняемому файлу Python (например, "python" или полный путь).
 * @param apiModuleName Имя модуля API (например, "backend.api").
 * @return Процесс бэкенда, если успешно запущен, иначе null.
 */
fun startBackendApi(
    pythonExecutable: String = "python",
    apiModuleName: String = "backend.api" // Теперь имя модуля API передается явно
): Process? {
    // Получаем текущую рабочую директорию приложения (вероятно, папка 'gui', так как Gradle запускает из нее).
    val currentWorkingDir = File(".").canonicalFile

    // Определяем корневую директорию проекта (родительская для 'gui').
    // Это директория, где находятся и 'backend', и 'gui'.
    val projectRootDirectory = currentWorkingDir.parentFile

    // Проверяем, что родительская директория существует и является директорией.
    if (projectRootDirectory == null || !projectRootDirectory.exists() || !projectRootDirectory.isDirectory) {
        val errorMessage = "Ошибка: Корневая директория проекта не найдена или не является директорией: ${projectRootDirectory?.canonicalPath}"
        println(errorMessage)
        return null
    }

    // Проверяем существование файла api.py
    val apiFileCheck = File(projectRootDirectory, apiModuleName.replace('.', File.separatorChar) + ".py")
    if (!apiFileCheck.exists()) {
        val errorMessage = "Ошибка: Файл API модуля не найден: ${apiFileCheck.canonicalPath}"
        println(errorMessage)
        return null
    }

    // Команда для запуска Python-модуля.
    // Важно: твой backend/api.py должен содержать логику для запуска сервера
    // (например, uvicorn.run(app, host="...", port=...) внутри if __name__ == "__main__":)
    val command = listOf(
        pythonExecutable,
        "-m", // Запускаем как модуль
        apiModuleName // Имя модуля, например "backend.api"
    )

    val processBuilder = ProcessBuilder(command)
    // !!! КЛЮЧЕВОЙ МОМЕНТ !!! Устанавливаем рабочую директорию для команды ProcessBuilder
    // на корневую директорию проекта, чтобы Python мог найти 'backend' как пакет.
    processBuilder.directory(projectRootDirectory)
    // Объединяем стандартный вывод и вывод ошибок, чтобы все шло в один поток.
    processBuilder.redirectErrorStream(true)

    println("Запуск бэкенда командой: ${command.joinToString(" ")} из директории: ${processBuilder.directory().canonicalPath}")

    try {
        val process = processBuilder.start()

        // Чтение вывода бэкенда в фоновом потоке, чтобы не блокировать основной поток GUI
        val reader = process.inputStream.bufferedReader()
        Thread {
            try {
                reader.forEachLine { line ->
                    println("BACKEND_LOG: $line")
                }
            } catch (e: Exception) {
                System.err.println("Ошибка чтения вывода бэкенда: ${e.message}")
            }
        }.start()

        // Добавляем хук завершения для остановки бэкенда при закрытии GUI
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Остановка процесса бэкенда...")
            process.destroy() // Пытаемся корректно завершить процесс
            try {
                process.waitFor(5, TimeUnit.SECONDS) // Ждем немного
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            if (process.isAlive) {
                process.destroyForcibly() // Если не завершился, убиваем принудительно
            }
            println("Процесс бэкенда остановлен.")
        })

        println("Бэкенд запущен в фоновом режиме.")
        return process // Возвращаем процесс, чтобы можно было управлять им, если нужно
    } catch (e: Exception) {
        System.err.println("Не удалось запустить бэкенд: ${e.message}")
        return null
    }
}