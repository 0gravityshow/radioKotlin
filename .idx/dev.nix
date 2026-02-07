{ pkgs, ... }: {
  channel = "stable-24.05";
  
  packages = [
    pkgs.jdk17
    pkgs.gradle
    pkgs.nodejs_20
  ];

  idx = {
    extensions = [
      "Dart-Code.flutter"
      "msjsdiag.debugger-for-chrome"
    ];
    
    workspace = {
      onCreate = {
        npm-install = "cd functions && npm install";
      };
    };
    
    previews = {
      enable = true;
      previews = {
        android = {
          command = ["./gradlew", "assembleDebug"];
          manager = "android";
        };
      };
    };
  };
  
  android = {
    enable = true;
    flutter.enable = false;
    googleApis.enable = true;
    googlePlayServices.enable = true;
    
    # Android SDK settings
    platforms = ["android-35"];
    buildTools = ["35.0.0"];
    systemImages = ["system-images;android-35;google_apis;x86_64"];
    
    # Enable emulator
    emulator = {
      enable = true;
      device = "pixel_8";
    };
  };
}
