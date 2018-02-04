//
//  FeedView.swift
//  raa-ios-player
//
//  Created by Hamid on 1/24/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class FeedViewController : UIViewController {

    @IBOutlet var player: PlayerView!;
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

extension FeedViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        (self.publicFeedData, self.personalFeedData) = data as! ([PublicFeedEntry]?, Any?)
        publicFeedProgramCardTableView?.reloadData()
    }
}

extension FeedViewController : UITableViewDelegate {
    
}

extension FeedViewController : UITableViewDataSource {
    
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
        
        let programDetails = storyboard?.instantiateViewController(withIdentifier: "ProgramContent") as! ProgramDetailsViewController
        programDetails.program = feedEntry?.ProgramObject
        cell.card?.shouldPresent(programDetails, from: self)

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

        cell.card?.backgroundImage = #imageLiteral(resourceName: "default-thumbnail")
        
        // Fetches the appropriate meal for the data source layout.
        
        return cell
    }
}
