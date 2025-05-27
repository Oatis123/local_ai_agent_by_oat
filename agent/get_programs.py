import winreg

def get_installed_programs():
    uninstall_keys = [
        r"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall",
        r"SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall"
    ]
    programs = []
    for root in [winreg.HKEY_LOCAL_MACHINE, winreg.HKEY_CURRENT_USER]:
        for key in uninstall_keys:
            try:
                reg_key = winreg.OpenKey(root, key)
                for i in range(0, winreg.QueryInfoKey(reg_key)[0]):
                    sub_key = winreg.EnumKey(reg_key, i)
                    sub_key_path = f"{key}\\{sub_key}"
                    subkey = winreg.OpenKey(root, sub_key_path)
                    try:
                        name = winreg.QueryValueEx(subkey, "DisplayName")[0]
                        try:
                            path = winreg.QueryValueEx(subkey, "InstallLocation")[0]
                        except:
                            path = None
                        programs.append((name.lower(), path))
                    except:
                        continue
            except:
                continue
    programs.sort(key=lambda x: x[0])
    return dict(programs)

print(get_installed_programs())