$window = $CmdLine[1]
$folder = $CmdLine[2]

$OS = @OSVersion

_WaitForWindow($window)
_SetAddress($window, $folder)
_SelectFiles($window)

;verify if window with the specified title exists. If window is not appeared after some time, then exit autoit script
Func _WaitForWindow($windowTitle)
   For $i = 1 to 3 Step 1
	  Sleep(200)
	  If WinExists($window, "") = 1 Then ExitLoop
	  If $i=3 Then Exit
   Next
EndFunc

;set address into address bar and press	"Go To" button  
Func _SetAddress($windowTitle, $folderPath)
   Sleep(500)
   If $OS = "WIN_2008R2" Then
	  ControlClick($windowTitle, "", "ToolbarWindow322", "left", 1, 260, 10) ;click on the address bar on Win 2008 Server
      ElseIf $OS = "WIN_7" Then
		 ControlClick($windowTitle, "", "ToolbarWindow322", "left", 1, 600,10) ;click on the address bar on Win 7
   EndIf
   Sleep(500)
   ControlSetText($windowTitle, "", "Edit2", "") ;clear text from the address bar
   Sleep(100)
   ControlSetText($windowTitle, "", "Edit2", $folderPath) ;specify the folder where files are located
   Sleep(200)
   If $OS = "WIN_2008R2" Then
	  ControlClick($windowTitle, "", "ToolbarWindow323") ;press "Go to" button on Win 2008 Server
	  ElseIf $OS = "WIN_7" Then
		 ControlClick($windowTitle, "", "ToolbarWindow323") ;press "Go to" button on Win 7
   EndIf
   Sleep(500)
EndFunc

;specify filenames in the format: "File1" "File2"
Func _SelectFiles($windowTitle)
   $string = _GetFileNames()
   ControlSetText($windowTitle, "", "Edit1", $string); specify files
	  Sleep(500)
	  ControlClick($windowTitle, "", "Button1")
	  Sleep(300)
   EndFunc   

;get filenames from array of command line parameters and build a string with these names in the format: "File1" "File2"
Func _GetFileNames()
   $numberOfFiles = 0
   $string = ''
   Local $i = 3
   Do
	  $string = $string & '"' & $CmdLine[$i] &'" '
	  $i=$i+1
	  $numberOfFiles=$numberOfFiles+1
   Until $i = UBound($CmdLine)
   Return $string
EndFunc
   