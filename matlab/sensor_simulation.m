for i = 1:100
    azimuth = randi([0, 180]);
    tilt = randi([0, 180]);
    energyProduced = 10 + rand() * 5;
    energyRemaining = max(0, 100 - energyProduced);
    dustLevel = randi([100, 400]);

    % Open the file in append mode ('a') to add new data without overwriting
    fileID = fopen(filePath, 'a'); 
    fprintf(fileID, '%d,%d,%.2f,%.2f,%d\n', azimuth, tilt, energyProduced, energyRemaining, dustLevel);
    fclose(fileID);

    disp(['Updated: ', datestr(now), ' -> Azimuth: ', num2str(azimuth), ', Tilt: ', num2str(tilt)]);
    pause(5);

end
