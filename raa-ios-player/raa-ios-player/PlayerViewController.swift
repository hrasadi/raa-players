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
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        let attributes: [String: AnyObject] = [NSFontAttributeName:UIFont(name: "BRoya", size: 18)!]
        let appearance = UIBarButtonItem.appearance()
        appearance.setTitleTextAttributes(attributes, for: .normal)
        
        programList.delegate = self
        programList.dataSource = self
        
        DispatchQueue.main.async {
            Settings.loadLineup()
            self.programList.reloadData()
        }
        
        Settings.getPlaybackManager().playbackStopCallback = onPlaybackEnd
        Settings.getPlaybackManager().programStartCallback = onProgramStart

    }

    @IBAction func onPodcastsButtonItemClicked(_ sender: Any) {
        let podcastURL = URL(string: "pcast://itunes.apple.com/us/podcast/%D8%B1%D8%A7%D8%AF%DB%8C%D9%88-%D8%A7%D8%AA%D9%88-%D8%A7%D8%B3%D8%B9%D8%AF/id1266849225?mt=2")
        
        UIApplication.shared.open(podcastURL!);
    }
    
    func onProgramStart(_ currentProgramTitle: String?) {
        playbackStatusLabel.text = "در حال پخش: " + currentProgramTitle!
    }
    
    func onPlaybackEnd(_ nextBoxId: String?, boxStartTime: Date?) {
        if (nextBoxId == nil) {
            playbackStatusLabel.text = "شب بخیر! ادامه‌ی برنامه‌های رادیو از نیمه شب..."
        } else {
            var counter = Int(boxStartTime!.timeIntervalSince(Date()))
            if (self.playbackCountDown == nil) {
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
                    }
                }                
            }
        }
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
        cell.programName.font = UIFont(name: "B Roya", size: 15)!

        
        // Get rid of HTML tags
        do {
            let clips : String = ((Settings.getLineup()?["array"] as! [Any])[indexPath.row] as! Dictionary)["description"]!
            
            let attrStr = try NSAttributedString(
                data: clips.data(using: String.Encoding.unicode, allowLossyConversion: true)!,
                options: [NSDocumentTypeDocumentAttribute: NSHTMLTextDocumentType],
                documentAttributes: nil)
            cell.programClips.text = attrStr.string
        } catch _ {
            
        }
        cell.programClips.textAlignment = .right
        cell.programClips.font = UIFont(name: "B Roya", size: 11)!

        cell.programTime.text =
            ((Settings.getLineup()?["array"] as! [Any])[indexPath.row] as! Dictionary)["startTime"]!
            + "-" +
            ((Settings.getLineup()?["array"] as! [Any])[indexPath.row] as! Dictionary)["endTime"]!
        cell.programTime.textAlignment = .right
        cell.programTime.font = UIFont(name: "B Roya", size: 15)!

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

