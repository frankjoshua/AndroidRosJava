@echo on

if [%ARDUINO_HOME%]==[] goto ERROR

copy .\TargetRegistration\*.* "%ARDUINO_HOME%\libraries\TargetRegistration\"

goto EXIT

:ERROR
echo ARDUINO_HOME must be set

:EXIT