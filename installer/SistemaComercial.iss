#define MyAppName "Sistema Comercial"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Sistema Comercial"
#define MyAppExeName "start-sistema.bat"

[Setup]
AppId={{B6B8EBD4-6C88-4B7B-AF17-7E2A2D08C5CC}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={localappdata}\SistemaComercial
DefaultGroupName={#MyAppName}
OutputDir=output
OutputBaseFilename=SistemaComercial-Setup
Compression=lzma2
SolidCompression=yes
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64
UninstallDisplayIcon={app}\java\bin\java.exe

[Files]
Source: "payload\app\*"; DestDir: "{app}\app"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "payload\java\*"; DestDir: "{app}\java"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "payload\mysql\*"; DestDir: "{app}\mysql"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "payload\vcredist_x64.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\setup.ps1"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\backup.ps1"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\uninstall.ps1"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\start-sistema.bat"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"

[UninstallRun]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\uninstall.ps1"""; Flags: runhidden waituntilterminated

[Run]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\setup.ps1"" -AdminPassword ""{code:GetAdminPassword}"""; Flags: runhidden waituntilterminated
Filename: "{app}\{#MyAppExeName}"; Description: "Iniciar Sistema Comercial"; Flags: postinstall nowait skipifsilent

[Code]
var
  AdminPasswordPage: TInputQueryWizardPage;

procedure InitializeWizard;
begin
  AdminPasswordPage := CreateInputQueryPage(wpSelectDir,
    'Administrador inicial', 'Definí la contraseña inicial',
    'Se creará el usuario admin para el primer ingreso.');
  AdminPasswordPage.Add('Contraseña (mínimo 8 caracteres):', True);
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  if CurPageID = AdminPasswordPage.ID then
  begin
    if Length(AdminPasswordPage.Values[0]) < 8 then
    begin
      MsgBox('La contraseña debe tener al menos 8 caracteres.', mbError, MB_OK);
      Result := False;
    end;
    if Pos('"', AdminPasswordPage.Values[0]) > 0 then
    begin
      MsgBox('La contraseña no puede contener comillas dobles.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

function GetAdminPassword(Param: String): String;
begin
  Result := AdminPasswordPage.Values[0];
end;
