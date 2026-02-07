{ pkgs, ... }: {
  channel = "stable-24.05";
  
  packages = [
    pkgs.jdk17
    pkgs.nodejs_20
  ];

  env = {};
  
  idx = {
    extensions = [
      "fwcd.kotlin"
    ];
    
    workspace = {
      onCreate = {
        npm-install = "cd functions && npm install";
      };
    };
    
    previews = {
      enable = true;
      previews = {};
    };
  };
  
  android = {
    enable = true;
    flutter.enable = false;
    googleApis.enable = true;
    googlePlayServices.enable = true;
  };
}
