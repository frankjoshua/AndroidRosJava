@echo on

rem if [~%ARDUINO_HOME%]==[] goto ERROR

copy .\TargetRegistration\*.* "%ARDUINO_HOME%\libraries\"

goto EXIT

:ERROR
echo ARDUINO_HOME must be set

:EXIT