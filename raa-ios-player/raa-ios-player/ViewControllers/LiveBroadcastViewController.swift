//
//  LiveViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 1/25/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class LiveBroadcastContainerViewController : PlayerViewController {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}

class LiveBroadcastViewController : UIViewController {
    @IBOutlet var liveBroadcastProgramCardTableView: UITableView?
    
    private var liveLineupData: [CProgram]?
    
    var playbackCountdown: Timer? = nil
    var currentActionableCellIndexPath: IndexPath? = nil
    var currentNextInLineCountdownValue: String? = nil
        
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        self.liveBroadcastProgramCardTableView?.dataSource = self
        self.liveBroadcastProgramCardTableView?.delegate = self

        Context.Instance.liveBroadcastManager.registerEventListener(listenerObject: self)
        self.liveLineupData = Context.Instance.liveBroadcastManager.pullData() as? [CProgram]
        if (self.liveLineupData != nil) {
            self.liveBroadcastProgramCardTableView?.reloadData()
            self.scrollToCurrentProgram()
            self.updateTablePlayableStates() // Change cards state
        }
    }
    
    func scrollToCurrentProgram() {
        let indexPath = NSIndexPath(item: Context.Instance.liveBroadcastManager.getMostRecentProgramIndex() ?? 0, section: 0)

        // The delay is to address a bug in iOS
        // See: https://stackoverflow.com/questions/38611617/scrolltorowatindexpath-not-scrolling-correctly-to-the-bottom-on-ios-8
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            self.liveBroadcastProgramCardTableView?.scrollToRow(at: indexPath as IndexPath, at: UITableViewScrollPosition.middle, animated: true)
        }
    }
    
    func updateTablePlayableStates() {
        // Logic mismatch with server
        if Context.Instance.liveBroadcastManager.getMostRecentProgramIndex() == nil {
            return
        }
        
        let newActionableCellIndex = (Context.Instance.liveBroadcastManager.liveBroadcastStatus?.IsCurrentlyPlaying!)! ? Context.Instance.liveBroadcastManager.getMostRecentProgramIndex()! : Context.Instance.liveBroadcastManager.getMostRecentProgramIndex()! + 1

        if (currentActionableCellIndexPath?.row != newActionableCellIndex) {
            // Deregister old cell
            // Prepare for reassignment
            self.currentActionableCellIndexPath = nil
        }
        
        if (currentActionableCellIndexPath == nil) {
            currentActionableCellIndexPath = IndexPath(row: newActionableCellIndex, section: 0)
        }
        
        initiateCountdownIfNeeded()
        
        // Update current cell if it is visible
        if let cell = self.liveBroadcastProgramCardTableView?.cellForRow(at: currentActionableCellIndexPath!) as? LiveBroadcastProgramCardTableViewCell {
            setCellPlaybleStatus(forCellAt: self.currentActionableCellIndexPath!, cellObject: cell)
        }
    }
    
    func setCellPlaybleStatus(forCellAt indexPath: IndexPath, cellObject cell: LiveBroadcastProgramCardTableViewCell) {

        if (indexPath.row == self.currentActionableCellIndexPath?.row) {
            // We now have a new actionable cell. Update its playable status
            if Context.Instance.liveBroadcastManager.liveBroadcastStatus?.IsCurrentlyPlaying! == true {
                cell.card?.liveBroadcastPlayableState = .Playable
            } else {
                cell.card?.liveBroadcastPlayableState = .NextInLine
                cell.card?.nextInLineCountdownValue = self.currentNextInLineCountdownValue
            }
        } else {
            cell.card?.liveBroadcastPlayableState = .NotPlayable
        }
    }
    
    func initiateCountdownIfNeeded() {
        if Context.Instance.liveBroadcastManager.liveBroadcastStatus?.IsCurrentlyPlaying != nil && self.currentActionableCellIndexPath != nil {
            if let targetDateString = Context.Instance.liveBroadcastManager?.flattenLiveLineup[(self.currentActionableCellIndexPath)!.row + 1].Metadata?.StartTime {
                
                let targetDate = Formatter.iso8601.date(from: targetDateString)!
                var counter = Int(targetDate.timeIntervalSince(Date()))
                // Start time is in the past? This should not happen (unless there is something wrong with server. However, we should not show a negative counter. Stick with 'Soon..' label.
                if (counter <= 0) {
                    return
                }
                
                self.playbackCountdown = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
                    
                    let cell = self.liveBroadcastProgramCardTableView?.cellForRow(at: self.currentActionableCellIndexPath!) as? LiveBroadcastProgramCardTableViewCell
                    
                    counter -= 1
                    if counter == 0 {
                        self.playbackCountdown!.invalidate()
                        self.playbackCountdown = nil
                        
                        // display 'Soon...'
                        cell?.card?.nextInLineCountdownValue = nil
                        self.currentNextInLineCountdownValue = nil
                    } else {
                        var playbackCountdownString = ""
                        if (counter / 3600 != 0) {
                            playbackCountdownString = playbackCountdownString + String(counter / 3600) + " ساعت و "
                        }
                        var remaining = counter % 3600
                        if (remaining / 60 != 0) {
                            playbackCountdownString  = playbackCountdownString  + String(remaining / 60) + " دقیقه و "
                        }
                        remaining = remaining % 60
                        playbackCountdownString  = playbackCountdownString  + String(remaining) + " ثانیه "
                        
                        playbackCountdownString = Utils.convertToPersianLocaleString(playbackCountdownString)!
                        
                        cell?.card?.nextInLineCountdownValue = playbackCountdownString
                        self.currentNextInLineCountdownValue = playbackCountdownString
                    }
                }
            }
        }
    }
}

extension LiveBroadcastViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        self.liveLineupData = data as? [CProgram]
        self.liveBroadcastProgramCardTableView?.reloadData()
        self.scrollToCurrentProgram()
        self.updateTablePlayableStates() // Change cards state

    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

extension LiveBroadcastViewController : FeedEntryCardDelegate {
    func onPlayButtonClicked(_ requestedFeedEntryId: String) {
        Context.Instance.playbackManager.playFeed(requestedFeedEntryId)
    }
}

extension LiveBroadcastViewController : UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Defaults.CELL_HEIGHT
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.liveLineupData?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "liveProgramCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? LiveBroadcastProgramCardTableViewCell else {
            fatalError("The dequeued cell is not an instance of LiveBroadcastProgramCardTableViewCell.")
        }
        
        let program = liveLineupData?[indexPath.row]
        let programInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(program?.ProgramId)!]

        let programDetails = storyboard?.instantiateViewController(withIdentifier: "ProgramContent") as! ProgramDetailsViewController
        programDetails.program = program
        cell.card?.shouldPresent(programDetails, from: self)

        if (program?.Title != nil) {
            cell.card?.programTitle = (program?.Title)!
        }
        if (program?.Subtitle != nil) {
            cell.card?.programSubtitle = (program?.Subtitle)!
        }

        if (program?.Metadata?.StartTime != nil) {
            let startDate = Formatter.iso8601.date(from: (program?.Metadata?.StartTime)!)
            cell.card?.timeValue1 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: startDate)) ?? (cell.card?.timeValue1)!
            cell.card?.timeSubValue1 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(startDate)) ?? (cell.card?.timeSubValue1)!
        }

        if (program?.Metadata?.EndTime != nil) {
            let endDate = Formatter.iso8601.date(from: (program?.Metadata?.EndTime)!)
            cell.card?.timeValue2 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: endDate)) ?? (cell.card?.timeValue2)!
            cell.card?.timeSubValue2 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(endDate)) ?? (cell.card?.timeSubValue2)!
        }

        cell.card?.backgroundImage = UIImage(data: (programInfo?.bannerImageData ?? ProgramInfo.defaultBannerImageData)!)

        if Context.Instance.liveBroadcastManager.isProgramOver(programIndex: indexPath.row) {
            cell.card?.disable()
        } else {
            cell.card?.enable()
        }
        
        self.setCellPlaybleStatus(forCellAt: indexPath, cellObject: cell)
        
        cell.card?.setNeedsDisplay()

        //cell.card?.feedDelegate = self

        return cell
    }
}

extension Formatter {
    static let iso8601: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()
}
