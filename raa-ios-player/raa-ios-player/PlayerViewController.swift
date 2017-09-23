//
//  FirstViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 9/7/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit
import Foundation
import MediaPlayer

class PlayerViewController: UIViewController {
    
    @IBOutlet weak var programList: UITableView!
    @IBOutlet weak var playbackStatusLabel: UILabel!

    var playbackCountDown: Timer? = nil;
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
                
        // we should also handle MPInfoCenter callbacks
        self.becomeFirstResponder()
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        programList.delegate = self
        programList.dataSource = self
        
        Settings.getPlaybackManager().playbackStopCallback = onPlaybackEnd
        Settings.getPlaybackManager().programStartCallback = onProgramStart
    }
    
    func reloadLineup() {
        DispatchQueue.main.async {
            Settings.loadLineup()
            self.programList.reloadData()
        }
    }

    @IBAction func onPodcastsButtonItemClicked(_ sender: Any) {
        let podcastURL = URL(string: "https://itunes.apple.com/us/podcast/%D8%B1%D8%A7%D8%AF%DB%8C%D9%88-%D8%A7%D8%AA%D9%88-%D8%A7%D8%B3%D8%B9%D8%AF/id1266849225?mt=2")
        
        UIApplication.shared.open(podcastURL!);
    }
    
    func onProgramStart(_ currentProgramTitle: String?) {
        // In some cases, the playbackCountDown might not be invalidated properly (because of time skew, etc). If we are informed of a playback start, we should also invalidate any remaining playbackCountDown timers.
        self.playbackCountDown?.invalidate()
        self.playbackCountDown = nil
        
        // Update the status label
        playbackStatusLabel.text = "در حال پخش: " + currentProgramTitle!
        
        // Good time to redraw the program list as well
        self.programList.reloadData()
    }
    
    func onPlaybackEnd(_ nextBoxId: String?, boxStartTime: Date?) {
        if (nextBoxId == nil) {
            playbackStatusLabel.text = "شب بخیر! ادامه‌ی برنامه‌های رادیو از نیمه شب..."
        } else {
            var counter = Int(boxStartTime!.timeIntervalSince(Date()))
            if (self.playbackCountDown != nil) {
                self.playbackCountDown!.invalidate()
                self.playbackCountDown = nil
            }
            self.playbackCountDown = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
                counter -= 1
                if counter == 0 {
                    self.playbackCountDown!.invalidate()
                    self.playbackCountDown = nil
                    
                    self.playbackStatusLabel.text = "به زودی: " + nextBoxId!
                } else {
                    self.playbackStatusLabel.text = nextBoxId! + " در "
                    
                    if (counter / 3600 != 0) {
                        self.playbackStatusLabel.text = self.playbackStatusLabel.text! + String(counter / 3600) + " ساعت و "
                    }
                    var remaining = counter % 3600
                    if (remaining / 60 != 0) {
                        self.playbackStatusLabel.text = self.playbackStatusLabel.text! + String(remaining / 60) + " دقیقه و "
                    }
                    remaining = remaining % 60
                    self.playbackStatusLabel.text = self.playbackStatusLabel.text! + String(remaining) + " ثانیه "
                    
                    self.playbackStatusLabel.text = Settings.convertToPersianLocaleString(self.playbackStatusLabel.text)
                }
            }
        }
        // Good time to redraw the program list as well
        self.programList.reloadData()
    }
    
    @objc override func remoteControlReceived(with event: UIEvent?) {
        let rc: UIEventSubtype = event!.subtype
        
        if (rc == .remoteControlPlay) {
            Settings.getPlaybackManager().play()
        } else if (rc == .remoteControlPause) {
            Settings.getPlaybackManager().stop()
            
            MPNowPlayingInfoCenter.default().nowPlayingInfo?[MPNowPlayingInfoPropertyPlaybackRate] = 0
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

extension PlayerViewController: UITableViewDataSource, UITableViewDelegate {
    
    // Define no of rows in your tableView
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let lineupArray = Settings.getLineup()?["array"] {
            return (lineupArray as! [Any]).count
        }
        else {
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        let cell = tableView.dequeueReusableCell(withIdentifier: "ProgramCell", for: indexPath) as! ProgramCell
        
        cell.programName.text = ((Settings.getLineup()?["array"] as! [Any])[indexPath.row] as! Dictionary)["title"]!
        cell.programName.textAlignment = .right
        cell.programName.font = UIFont(name: ".SF UI Text", size: 15)!

        
        // Get rid of HTML tags
        do {
            let clips : String = ((Settings.getLineup()?["array"] as! [Any])[indexPath.row] as! Dictionary)["description"]!
            
            let attrStr = try NSAttributedString(
                data: clips.data(using: String.Encoding.unicode, allowLossyConversion: true)!,
                options: [NSAttributedString.DocumentReadingOptionKey.documentType: NSAttributedString.DocumentType.html],
                documentAttributes: nil)
            cell.programClips.text = attrStr.string
        } catch let e {
            NSLog(e.localizedDescription)
        }
        
        cell.programClips.textAlignment = .right
        cell.programClips.font = UIFont(name: ".SF UI Text", size: 12)!
        cell.programClips.lineBreakMode = .byTruncatingTail
            
        cell.programTime.text = Settings.convertToPersianLocaleString(
            ((Settings.getLineup()?["array"] as! [Any])[indexPath.row] as! Dictionary)["startTime"]!
            + "-" +
            ((Settings.getLineup()?["array"] as! [Any])[indexPath.row] as! Dictionary)["endTime"]!)
        cell.programTime.textAlignment = .right
        cell.programTime.font = UIFont(name: ".SF UI Text", size: 14)!

        return cell;
    }
    
    func tableView(_ tableView: UITableView, shouldHighlightRowAt indexPath: IndexPath) -> Bool {
        return false
    }
    
}

class ProgramCell : UITableViewCell {
    
    @IBOutlet weak var programName: UILabel!
    @IBOutlet weak var programClips: UILabel!
    @IBOutlet weak var programTime: UILabel!
}

