#include <IE.au3>
#include <WinAPI.au3>

if $CmdLine[0] < 1 Then
; Arguments are not enough
MsgBox(4096, "Error","Supply all the arguments:" & @LF & @LF &"Required:"& @LF &"- Path to the folder" & @LF & @LF &"Optional:"  & @LF & "- Time to show window (ms) (Default is 2500 ms)" & @LF & "- Dialog title (default is: File Window)" & @LF & "- Window Heigth (default is 500)" & @LF & "- Window Width (default is 500)" & @LF & @LF &"Use ""null"" (in quotes) to skip certain parameter", 10)
Exit
EndIf
Local $windowHeigth = 500
Local $windowWidth = 500
Local $waitTime = 2500
Local $fileWindowTitle = "File Window"

If($CmdLine[0] >= 2) Then
   $waitTime = $CmdLine[2]
   If $CmdLine[2] = "null" Then
	  $waitTime = 2500
   EndIf
EndIf

If ($CmdLine[0]>=3) Then
   $fileWindowTitle = $CmdLine[3]
   If $CmdLine[3] = "null" Then
	  $fileWindowTitle = "File Window"
   EndIf
   
EndIf

If($CmdLine[0] >= 4) Then
   $windowHeigth = $CmdLine[4]
   If $CmdLine[4] = "null" Then
	  $windowHeigth = 500
   EndIf
EndIf

If($CmdLine[0] = 5) Then
   $windowWidth = $CmdLine[5]
   If $CmdLine[5] = "null" Then
	  $windowWidth = 500
   EndIf
EndIf

Local $folderPath = $CmdLine[1]

$oIE = _IECreateEmbedded()
; Create a simple GUI for our output
GUICreate($fileWindowTitle, $windowWidth, $windowHeigth)
$GUIActiveX = GUICtrlCreateObj( $oIE, 0, 0 , $windowWidth, $windowHeigth )
$handle = WinGetHandle($fileWindowTitle)
_WinAPI_SetWindowPos($handle, 1, (@DesktopWidth-$windowWidth), 5, $windowWidth, $windowHeigth , 1)
_IENavigate($oIE, $folderPath, 0)
GUISetState()

Sleep($waitTime)

GUIDelete()