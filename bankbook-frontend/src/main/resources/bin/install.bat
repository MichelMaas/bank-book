@echo off
pushd %CD%\..
set DIRECTORY=%CD%
popd

Shortcut.exe /F:"%AppData%\Microsoft\Windows\Start Menu\Programs\FX-Analyzer.lnk" /A:C /W:"%DIRECTORY%\bin" /T:"%DIRECTORY%\bin\start.bat" /I:"%DIRECTORY%\lib\fx-analyzer.ico,0"