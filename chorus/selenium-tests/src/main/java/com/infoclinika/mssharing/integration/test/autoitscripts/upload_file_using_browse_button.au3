if $CmdLine[0] < 3 then
; Arguments are not enough message
msgbox(0,"Error","Supply all the arguments: Dialog title, Path to the folder, File names(at least one)")
Exit
EndIf

$OS = @OSVersion

if $OS = "WIN_2008" Then
      While 1
	  Sleep(100)
	  If WinExists($CmdLine[1], "") = 1 Then ExitLoop
   WEnd
   Sleep(500)
   $title = WinGetTitle($CmdLine[1]) ; retrives whole window title
	  ControlClick($title, "", "ToolbarWindow323", "left", 1, 8,9)
	  Sleep(100)
	  ControlSetText($title, "", "Edit2", "")
	  Sleep(100)
	  ControlSend($title, "", "Edit2", $CmdLine[2]) ;specify the folder where files are located
	  Sleep(200)
	  ControlClick($title, "", "ToolbarWindow324")
	  Sleep(500)
	  $numberOfFiles = 0
   $string = ''
   Local $i = 3
   Do
	  $string = $string & '"' & $CmdLine[$i] &'" '
	  $i=$i+1
	  $numberOfFiles=$numberOfFiles+1
   Until $i = UBound($CmdLine)
	  Sleep(300)
   ControlSend($title, "", "Edit1", $string)
	  Sleep(500)
	  ControlClick($title, "", "Button1")
	  Sleep(300)
EndIf
   
if $OS = "WIN_7" Then   
   ; waits for window existing
   While 1
	  Sleep(100)
	  If WinExists($CmdLine[1], "") = 1 Then ExitLoop
   WEnd
   Sleep(500)
   $title = WinGetTitle($CmdLine[1]) ; retrives whole window title
	  ControlClick($title, "", "ToolbarWindow322", "left", 1, 8,9)
	  Sleep(100)
	  ControlSend($title, "", "Edit2", $CmdLine[2]) ;specify the folder where files are located
	  ControlClick($title, "", "ToolbarWindow323")
	  Sleep(500)
	  $numberOfFiles = 0
   $string = ''
   Local $i = 3
   Do
	  $string = $string & '"' & $CmdLine[$i] &'" '
	  $i=$i+1
	  $numberOfFiles=$numberOfFiles+1
   Until $i = UBound($CmdLine)
	  Sleep(300)
   Send("{CTRLDOWN}") 
$yCoord = 43   
   For $i = 1 to $numberOfFiles Step 1
	  ControlClick($title, "", "DirectUIHWND2", "", 1, 81, $yCoord)
	  $yCoord=$yCoord + 20
   Next
   Send("{CTRLUP}")
	  Sleep(500)
	  ControlClick($title, "", "Button1")
	  Sleep(300)
	  
EndIf


   
