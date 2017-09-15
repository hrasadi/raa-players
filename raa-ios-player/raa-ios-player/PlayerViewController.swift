//
//  FirstViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 9/7/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit
import AVFoundation
import Foundation

class PlayerViewController: UIViewController {

    var player: AVPlayer? = nil
    
    @IBOutlet weak var programList: UITableView!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        player = AVPlayer.init(url: URL.init(string: "https://stream.raa.media/raa1.ogg")!)
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
    }

    @IBAction func onPodcastsButtonItemClicked(_ sender: Any) {
        let podcastURL = URL(string: "pcast://itunes.apple.com/us/podcast/%D8%B1%D8%A7%D8%AF%DB%8C%D9%88-%D8%A7%D8%AA%D9%88-%D8%A7%D8%B3%D8%B9%D8%AF/id1266849225?mt=2")
        
        UIApplication.shared.open(podcastURL!);
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func onRadioPowerSwitchChanged(_ sender: UISwitch) {
        if (sender.isOn) {
            player!.play();
        }
        else {
            player!.pause();
        }
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
    
}


class ProgramCell : UITableViewCell {
    
    @IBOutlet weak var programName: UILabel!
    @IBOutlet weak var programClips: UILabel!
    @IBOutlet weak var programTime: UILabel!
}

