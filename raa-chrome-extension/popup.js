
document.addEventListener('DOMContentLoaded', function() {
    chrome.runtime.sendMessage({getRadioPowerSwitch: null}, function(response) {
        document.getElementById('radioSwitch').checked = response.radioPowerSwitch;        
        if (response.radioPowerSwitch) {
            chrome.runtime.sendMessage({getCurrentProgram: null}, function(response) {
                jQuery('#currentProgram').html(response.currentProgram);
            });
        }
    });
    
    document.getElementById('radioSwitch').addEventListener('change', function(event) {
        chrome.runtime.sendMessage({setRadioPowerSwitch: event.target.checked});
        if (event.target.checked) {
            chrome.runtime.sendMessage({getCurrentProgram: null}, function(response) {
                jQuery('#currentProgram').html(response.currentProgram);
            });
        } else {
            jQuery('#currentProgram').html('');
        }
    });
});



