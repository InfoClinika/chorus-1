if $CmdLine[0] < 1 Then
; Arguments are not enough message
MsgBox(4096, "Error","Supply all the arguments:" & @LF & @LF &"Required:"& @LF &"- Number of files to drag'n'drop (up to 12 for source window with width 500, or up to 6 with another width)" & @LF & @LF &"Optional:"  & @LF & "- Souce Window title (default is: File Window)" & @LF & "- Target Window title (default is: Chorus – Dashboard)" & @LF & @LF & "Use ""null"" (in quotes) to skip certain parameter", 10)
Exit
EndIf

$OS = @OSVersion

Opt("WinTitleMatchMode", 2)

;Local $numberOfFiles = $CmdLine[1]
Local $sourceWindowTitle = "File Window"
Local $targetWindowTitle = "Chorus"
$numberOfFiles = $CmdLine[1]

If ($CmdLine[0] >= 2) Then 
   $sourceWindowTitle = $CmdLine[2]
   If $CmdLine[2] = "null" Then
	  $sourceWindowTitle = "File Window"
   EndIf
EndIf

If ($CmdLine[0] >= 3) Then 
   $targetWindowTitle = $CmdLine[3]
   If $CmdLine[3] = "null" Then
	  $targetWindowTitle = "Chorus"
   EndIf
EndIf

if $OS = "WIN_7" Then

WinWait($sourceWindowTitle)
$sourceWindowSize = ControlGetPos($sourceWindowTitle, "", "SysListView321")

$targetWindowTitleFull = WinGetTitle($targetWindowTitle)


$posSourceWindow = WinGetPos($sourceWindowTitle)
$posTargetWindow = WinGetPos($targetWindowTitleFull)
$xSource = (($posSourceWindow[0]+2) +40)
$ySource = (($posSourceWindow[1] +20) + 67)
$xTarget = (($posTargetWindow[0]+$posTargetWindow[2])/2)-100
$yTarget = (($posTargetWindow[1]+$posTargetWindow[3])/2)+50


If ($numberOfFiles>6 AND ($sourceWindowSize[2] <> 500)) Then
   $numberOfFiles = 6
EndIf

If ($numberOfFiles>12 AND ($sourceWindowSize[2] = 500)) Then
   $numberOfFiles = 12
EndIf

Send("{CTRLDOWN}")

If $numberOfFiles = 1 Then
	MouseClickDrag ("left", $xSource, $ySource, $xTarget, $yTarget)
Else
For $i = 1 to $numberOfFiles Step 1
	if $i=7 Then
	  $xSource = (($posSourceWindow[0]+2) + 40)
	  $ySource = (($posSourceWindow[1] +20) + 167)
   EndIf
   	  MouseClick("",$xSource, $ySource)
	  $xSource = $xSource + 75
Next
Sleep(500)
MouseClickDrag ("left", $xSource-75, $ySource, $xTarget, $yTarget)
EndIf
Send("{CTRLUP}")

EndIf

If $OS = "WIN_2008" Then
   
WinWait($sourceWindowTitle)
$sourceWindowSize = ControlGetPos($sourceWindowTitle, "", "SysListView321")

$targetWindowTitleFull = WinGetTitle($targetWindowTitle)

$posSourceWindow = WinGetPos($sourceWindowTitle)
$posTargetWindow = WinGetPos($targetWindowTitleFull)
$xSource = (($posSourceWindow[0]+2) +3)
$ySource = (($posSourceWindow[1] +20) + 27)
$xTarget = (($posTargetWindow[0]+$posTargetWindow[2])/2)-100
$yTarget = (($posTargetWindow[1]+$posTargetWindow[3])/2)+50

$xAll = (($posSourceWindow[0]+2) + 50)

$fileNumber = $numberOfFiles - 1

MouseClickDrag ("left", $xSource, $ySource, $xSource + 20, $ySource + 18*$fileNumber)
Sleep(500)
MouseClickDrag("left", $xAll, $ySource, $xTarget, $yTarget)
   
EndIf

