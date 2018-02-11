//
//  FeedView.swift
//  raa-ios-player
//
//  Created by Hamid on 1/24/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import os

class FeedContainerViewController : PlayerViewController {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}

class FeedViewController : UIViewController {

    @IBOutlet var publicFeedProgramCardTableView: UITableView?

    private var publicFeedData: [PublicFeedEntry]?
    private var personalFeedData: Any?
    
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override func viewDidLoad() {
        super.viewDidLoad();
        
        Context.Instance.feedManager.registerEventListener(listenerObject: self)
        (self.publicFeedData, self.personalFeedData) = Context.Instance.feedManager.pullData() as! ([PublicFeedEntry]?, Any?)

        publicFeedProgramCardTableView?.dataSource = self
        publicFeedProgramCardTableView?.delegate = self
        
        publicFeedProgramCardTableView?.reloadData()
    }
}

extension FeedViewController : FeedCardDelegate {
    func onPlayButtonClicked(_ requestedFeedEntryId: String) {
        Context.Instance.playbackManager.playFeed(requestedFeedEntryId)
    }
}

extension FeedViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        (self.publicFeedData, self.personalFeedData) = data as! ([PublicFeedEntry]?, Any?)
        publicFeedProgramCardTableView?.reloadData()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

extension FeedViewController : UITableViewDataSource, UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Defaults.CELL_HEIGHT
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.publicFeedData?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "publicFeedCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? PublicFeedProgramCardTableViewCell else {
            fatalError("The dequeued cell is not an instance of PublicFeedProgramCardTableViewCell.")
        }
        
        let feedEntry = publicFeedData?[indexPath.row]
        let entryProgramInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(feedEntry?.ProgramObject?.ProgramId)!]

        let programDetails = storyboard?.instantiateViewController(withIdentifier: "ProgramContent") as! ProgramDetailsViewController
        programDetails.program = feedEntry?.ProgramObject
        cell.card?.shouldPresent(programDetails, from: self)

        cell.card?.feedEntryId = feedEntry?.Id
        
        if (feedEntry?.ProgramObject?.Title != nil) {
            cell.card?.programTitle = (feedEntry?.ProgramObject?.Title)!
        }
        
        if (feedEntry?.ReleaseTimestamp != nil) {
            let releaseDate = Date(timeIntervalSince1970: (feedEntry?.ReleaseTimestamp)!)
            cell.card?.timeValue1 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: releaseDate)) ?? (cell.card?.timeValue1)!
            cell.card?.timeSubValue1 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(releaseDate)) ?? (cell.card?.timeSubValue1)!
        }

        if (feedEntry?.ExpirationTimestamp != nil) {
            let expirationDate = Date(timeIntervalSince1970: (feedEntry?.ExpirationTimestamp)!)
            cell.card?.timeValue2 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: expirationDate)!) ?? (cell.card?.timeValue2)!
            cell.card?.timeSubValue2 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(expirationDate)) ?? (cell.card?.timeSubValue2)!
        }

        if (entryProgramInfo?.Banner != nil) {
            let bannerUrl = URL(string: entryProgramInfo!.Banner!)!
            let data = try? Data(contentsOf: bannerUrl)
            if (data != nil) {
                    cell.card?.backgroundImage = UIImage(data: data!)
            } else {
                cell.card?.backgroundImage = #imageLiteral(resourceName: "default-thumbnail")
            }
        } else {
            cell.card?.backgroundImage = #imageLiteral(resourceName: "default-thumbnail")
        }
        
        cell.card?.feedDelegate = self
        
        return cell
    }
}
