@echo off
chcp 65001 >nul
echo Сборка плагина Asonsss_manhunt...
call mvn clean package
echo.
if exist "target\Asonsss_manhunt-26.3.jar" (
    echo УСПЕШНО! Готовый файл плагина находится здесь:
    echo target\Asonsss_manhunt-26.3.jar
    echo Перетащите этот .jar файл в папку /plugins на вашем сервере!
) else (
    echo Ошибка сборки. Убедитесь, что у вас установлен Maven и JDK 21, либо откройте проект в IntelliJ IDEA.
)
pause
