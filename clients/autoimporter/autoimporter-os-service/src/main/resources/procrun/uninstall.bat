:: to download Procrun binaries and see documentation visit http://commons.apache.org/daemon/procrun.html

set CURRENT_PATH=%~dp0

::detect x86 or x64
echo off
IF PROCESSOR_ARCHITECTURE EQU "ia64" GOTO IS_ia64
IF PROCESSOR_ARCHITEW6432 EQU "ia64" GOTO IS_ia64
IF PROCESSOR_ARCHITECTURE EQU "amd64" GOTO IS_amd64
IF PROCESSOR_ARCHITEW6432 EQU "amd64" GOTO IS_amd64
IF DEFINED ProgramFiles(x86) GOTO IS_amd64
:IS_x86
set PR_INSTALL="%CURRENT_PATH%prunsrv.exe"
GOTO IS_x64End
:IS_amd64
set PR_INSTALL="%CURRENT_PATH%amd64\prunsrv.exe"
GOTO IS_x64End
:IS_ia64
set PR_INSTALL="%CURRENT_PATH%ia64\prunsrv.exe"
:IS_x64End
echo on

set SERVICE_NAME=ChorusAutoimporter

%PR_INSTALL% //DS//%SERVICE_NAME%