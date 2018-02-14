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

    @IBOutlet var feedTableView: UITableView?

    private static let PERSONAL_FEED_SECTION = 0
    private static let PUBLIC_FEED_SECTION = 1

    private var publicFeedEntries: [PublicFeedEntry]?
    private var personalFeedEntries: [PersonalFeedEntry]?
    
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override func viewDidLoad() {
        super.viewDidLoad();
        
        Context.Instance.feedManager.registerEventListener(listenerObject: self)
        (self.publicFeedEntries, self.personalFeedEntries) = Context.Instance.feedManager.pullData() as! ([PublicFeedEntry]?, [PersonalFeedEntry]?)

        feedTableView?.dataSource = self
        feedTableView?.delegate = self
        
        feedTableView?.reloadData()
    }
}

extension FeedViewController : FeedEntryCardDelegate {
    func onPlayButtonClicked(_ requestedFeedEntryId: String) {
        Context.Instance.playbackManager.playFeed(requestedFeedEntryId)
    }
}

extension FeedViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        (self.publicFeedEntries, self.personalFeedEntries) = data as! ([PublicFeedEntry]?, [PersonalFeedEntry]?)
        feedTableView?.reloadData()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

extension FeedViewController : UITableViewDataSource, UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Defaults.CELL_HEIGHT
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2 // Personal and Public
    }
    
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if section == FeedViewController.PUBLIC_FEED_SECTION {
            return "برنامه‌های عمومی"
        } else if section == FeedViewController.PERSONAL_FEED_SECTION {
            return "برنامه‌های مخصوص شما"
        }
        return nil // this should not happen
    }

    func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        view.semanticContentAttribute = .forceRightToLeft
    }

    func tableView(_ tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        if section == FeedViewController.PUBLIC_FEED_SECTION {
            if self.publicFeedEntries == nil || self.publicFeedEntries?.count == 0 {
                return "فعلا برنامه‌ای اینجا نیست"
            }
        } else if section == FeedViewController.PERSONAL_FEED_SECTION {
            if self.personalFeedEntries == nil || self.personalFeedEntries?.count == 0 {
                return "فعلا برنامه‌ای اینجا نیست"
            }
        }
        return nil
    }
    
    func tableView(_ tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        guard let footer = view as? UITableViewHeaderFooterView else { return }
        
        footer.textLabel?.font = UIFont.systemFont(ofSize: 12)
        footer.textLabel?.textColor = UIColor.gray
        footer.textLabel?.textAlignment = .center
        footer.textLabel?.frame = footer.frame
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == FeedViewController.PUBLIC_FEED_SECTION {
            return self.publicFeedEntries?.count ?? 0
        } else if section == FeedViewController.PERSONAL_FEED_SECTION {
            return self.personalFeedEntries?.count ?? 0
        }
        return 0 // This should not happen
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.section == FeedViewController.PUBLIC_FEED_SECTION {
            return generatePublicCell(tableView, indexPath: indexPath)
        } else {
            // Personal
            return generatePersonalCell(tableView, indexPath: indexPath)
        }
    }

    func generatePersonalCell(_ tableView: UITableView, indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "feedCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? FeedEntryCardTableViewCell else {
            fatalError("The dequeued cell is not an instance of FeedEntryCardTableViewCell.")
        }
        
        let feedEntry = personalFeedEntries?[indexPath.row]
        let entryProgramInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(feedEntry?.ProgramObject?.ProgramId)!]
        
        let programDetails = storyboard?.instantiateViewController(withIdentifier: "ProgramContent") as! ProgramDetailsViewController
        programDetails.program = feedEntry?.ProgramObject
        cell.card?.shouldPresent(programDetails, from: self)
        
        cell.card?.feedEntryId = feedEntry?.Id
        
        if (feedEntry?.ProgramObject?.Title != nil) {
            cell.card?.programTitle = (feedEntry?.ProgramObject?.Title)!
        }
        if (feedEntry?.ProgramObject?.Subtitle != nil) {
            cell.card?.programSubtitle = (feedEntry?.ProgramObject?.Subtitle)!
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
        
        cell.card?.backgroundImage = UIImage(data: entryProgramInfo?.bannerImageData ?? ProgramInfo.defaultBannerImageData)
        
        cell.card?.setNeedsDisplay()
        
        cell.card?.feedDelegate = self
        
        return cell
    }
    
    func generatePublicCell(_ tableView: UITableView, indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "feedCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? FeedEntryCardTableViewCell else {
            fatalError("The dequeued cell is not an instance of FeedEntryCardTableViewCell.")
        }
        
        let feedEntry = publicFeedEntries?[indexPath.row]
        let entryProgramInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(feedEntry?.ProgramObject?.ProgramId)!]
        
        let programDetails = storyboard?.instantiateViewController(withIdentifier: "ProgramContent") as! ProgramDetailsViewController
        programDetails.program = feedEntry?.ProgramObject
        cell.card?.shouldPresent(programDetails, from: self)
        
        cell.card?.feedEntryId = feedEntry?.Id
        
        if (feedEntry?.ProgramObject?.Title != nil) {
            cell.card?.programTitle = (feedEntry?.ProgramObject?.Title)!
        }
        if (feedEntry?.ProgramObject?.Subtitle != nil) {
            cell.card?.programSubtitle = (feedEntry?.ProgramObject?.Subtitle)!
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
        
        cell.card?.backgroundImage = UIImage(data: entryProgramInfo?.bannerImageData ?? ProgramInfo.defaultBannerImageData)
        
        cell.card?.setNeedsDisplay()
        
        cell.card?.feedDelegate = self
        
        return cell
    }
}
