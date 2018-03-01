//
//  LiveViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 1/25/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import PromiseKit
import UIKit

class LiveBroadcastContainerViewController : PlayerViewController {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}

class LiveBroadcastViewController : UIViewController {
    
    @IBOutlet weak var spinner: UIActivityIndicatorView!
    @IBOutlet var liveBroadcastProgramCardTableView: UITableView?
    
    private var liveLineupData: LiveLineupData?
    
    var playbackCountdown: Timer? = nil
    var currentActionableCellIndexPath: IndexPath? = nil
    var currentNextInLineCountdownValue: String? = nil

    private var programDetailsViewController: ProgramDetailsViewController?

    // Redraw the whole view
    private static let FULL_REDRAW_PERIOD: TimeInterval = 3600
    private var lastFullRedrawnTimestamp: TimeInterval?
    
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        NotificationCenter.default.addObserver(self, selector: #selector(rotated), name: NSNotification.Name.UIDeviceOrientationDidChange, object: nil)

        self.fullRedraw()
    }

    // FIXES A BUG THAT LAYOUTS TABLE WRONG IN IOS 10
    func fixTableViewInsets() {
        let zContentInsets = UIEdgeInsets.zero
        liveBroadcastProgramCardTableView?.contentInset = zContentInsets
        liveBroadcastProgramCardTableView?.scrollIndicatorInsets = zContentInsets
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        fixTableViewInsets()
    }
    // END BUG FIX

    @objc func rotated() {
        self.fullRedraw()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        let currentDate = Date().timeIntervalSince1970

        if self.lastFullRedrawnTimestamp != nil {
            if currentDate - self.lastFullRedrawnTimestamp! > LiveBroadcastViewController.FULL_REDRAW_PERIOD {
                self.fullRedraw()
            }
        }
        
        // Always referesh the countdown timer
        self.initiateCountdownIfNeeded()
    }
    
    private func fullRedraw() {
        self.lastFullRedrawnTimestamp = Date().timeIntervalSince1970
        
        self.programDetailsViewController = storyboard?.instantiateViewController(withIdentifier: "ProgramContent") as? ProgramDetailsViewController
        
        self.liveBroadcastProgramCardTableView?.dataSource = self
        self.liveBroadcastProgramCardTableView?.delegate = self
        
        self.spinner.startAnimating()
        self.view.bringSubview(toFront: self.spinner)
        self.spinner.center = UIScreen.main.bounds.center
        self.spinner.setNeedsLayout()
        
        firstly {
            Context.Instance.liveBroadcastManager.pullData()
            }.done { liveLineupData in
                self.liveLineupData = liveLineupData
                
                self.spinner.stopAnimating()
                self.spinner.hidesWhenStopped = true
                
                self.liveBroadcastProgramCardTableView?.reloadData()
                self.scrollToCurrentProgram()
                self.updateTablePlayableStates() // Change cards state
            }.ensure {
                Context.Instance.liveBroadcastManager.registerEventListener(listenerObject: self)
            }.catch {_ in
        }
    }
    
    func scrollToCurrentProgram() {
        let indexPath = NSIndexPath(item: Context.Instance.liveBroadcastManager.getMostRecentProgramIndex() ?? 0, section: 0)

        // The delay is to address a bug in iOS
        // See: https://stackoverflow.com/questions/38611617/scrolltorowatindexpath-not-scrolling-correctly-to-the-bottom-on-ios-8
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            self.liveBroadcastProgramCardTableView?.scrollToRow(at: indexPath as IndexPath, at: UITableViewScrollPosition.top, animated: false)
        }
    }
    
    func updateTablePlayableStates() {
        // Logic mismatch with server
        if Context.Instance.liveBroadcastManager.getMostRecentProgramIndex() == nil {
            return
        }
        
        let newActionableCellIndex = (Context.Instance.liveBroadcastManager.liveLineupData.liveBroadcastStatus?.IsCurrentlyPlaying!)! ? Context.Instance.liveBroadcastManager.getMostRecentProgramIndex()! : Context.Instance.liveBroadcastManager.getMostRecentProgramIndex()! + 1

        var oldActionableCellRow: Int? = nil
        if (self.currentActionableCellIndexPath?.row != newActionableCellIndex) {
            // Save it
            oldActionableCellRow = self.currentActionableCellIndexPath?.row
            // Prepare for reassignment
            self.currentActionableCellIndexPath = nil
        }
        
        if (currentActionableCellIndexPath == nil) {
            self.currentActionableCellIndexPath = IndexPath(row: newActionableCellIndex, section: 0)
        }
        
        // Grayout the old cells (all that was prior to the current active cell)
        let currentCellIndexPathRow = self.currentActionableCellIndexPath?.row
        if oldActionableCellRow != nil && currentCellIndexPathRow != nil {
            for cellRow in oldActionableCellRow!..<currentCellIndexPathRow! {
                let cellIndexPath = IndexPath(row: cellRow, section: (self.currentActionableCellIndexPath?.section)!)
                let timeoutedCell = self.liveBroadcastProgramCardTableView?.cellForRow(at: cellIndexPath) as? LiveBroadcastProgramCardTableViewCell
                if timeoutedCell != nil {
                    timeoutedCell?.card?.liveBroadcastPlayableState = .NotPlayable
                    timeoutedCell?.card?.disable()
                    timeoutedCell?.card?.setNeedsDisplay()
                }
            }
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
            if Context.Instance.liveBroadcastManager.liveLineupData.liveBroadcastStatus?.IsCurrentlyPlaying! == true {
                cell.card?.liveBroadcastPlayableState = .Playable
            } else {
                cell.card?.liveBroadcastPlayableState = .NextInLine
                cell.card?.nextInLineCountdownValue = self.currentNextInLineCountdownValue
            }
        } else {
            cell.card?.liveBroadcastPlayableState = .NotPlayable
        }
        
        cell.card?.setNeedsDisplay()
    }
    
    func initiateCountdownIfNeeded() {
        if Context.Instance.liveBroadcastManager.liveLineupData.liveBroadcastStatus?.IsCurrentlyPlaying != nil && self.currentActionableCellIndexPath != nil {
            if let targetDateString = Context.Instance.liveBroadcastManager?.liveLineupData.flattenLiveLineup![(self.currentActionableCellIndexPath)!.row].Metadata?.StartTime {
                
                let targetDate = Formatter.dateFromISO8601(from: targetDateString)!
                var counter = Int(targetDate.timeIntervalSince(Date()))
                // Start time is in the past? This should not happen (unless there is something wrong with server. However, we should not show a negative counter. Allow users to play stream
                if (counter <= 0) {
                    return
                }
                
                if (self.playbackCountdown != nil) {
                    // invalidate old timers
                    self.playbackCountdown?.invalidate()
                    self.playbackCountdown = nil
                }
                
                self.playbackCountdown = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
                    
                    let cell = self.liveBroadcastProgramCardTableView?.cellForRow(at: self.currentActionableCellIndexPath!) as? LiveBroadcastProgramCardTableViewCell
                    counter -= 1
                    if counter == 0 {
                        self.playbackCountdown!.invalidate()
                        self.playbackCountdown = nil
                        
                        // Move card to 'Playable' State
                        cell?.card?.liveBroadcastPlayableState = .Playable
                        cell?.card?.setNeedsDisplay()
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
        let needCompleteReload = (data as? LiveLineupData)?.flattenLiveLineup![0].CanonicalIdPath != self.liveLineupData?.flattenLiveLineup![0].CanonicalIdPath
        let needStatusUpdate = (data as? LiveLineupData)?.liveBroadcastStatus?.IsCurrentlyPlaying != self.liveLineupData?.liveBroadcastStatus?.IsCurrentlyPlaying || (data as? LiveLineupData)?.liveBroadcastStatus?.MostRecentProgram != self.liveLineupData?.liveBroadcastStatus?.MostRecentProgram

        self.liveLineupData = data as? LiveLineupData
        
        // Reload the whole table only of the lineup is completely updated
        // (e.g. date passed)
        if (needCompleteReload) {
            self.liveBroadcastProgramCardTableView?.reloadData()
            self.scrollToCurrentProgram()
        }
        if needStatusUpdate {
            self.updateTablePlayableStates() // Change cards state
        }
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

extension LiveBroadcastViewController : LiveBroadcastDelegate {
    func onPlayButtonClicked() {
        Context.Instance.playbackManager.playLiveBroadcast()
    }
}

extension LiveBroadcastViewController : UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Defaults.CELL_HEIGHT
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.liveLineupData?.flattenLiveLineup?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "liveProgramCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? LiveBroadcastProgramCardTableViewCell else {
            fatalError("The dequeued cell is not an instance of LiveBroadcastProgramCardTableViewCell.")
        }
        
        let program = liveLineupData?.flattenLiveLineup?[indexPath.row]
        let programInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(program?.ProgramId)!]

        cell.card?.programId = program?.ProgramId
        cell.card?.shouldPresent(self.programDetailsViewController, from: self)

        cell.card?.programTitle = (program?.Title) ?? ""
        cell.card?.programSubtitle = (program?.Subtitle) ?? ""

        if (program?.Metadata?.StartTime != nil) {
            let startDate = Formatter.dateFromISO8601(from: (program?.Metadata?.StartTime)!)
            cell.card?.timeValue1 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: startDate)) ?? (cell.card?.timeValue1)!
            cell.card?.timeSubValue1 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(startDate)) ?? (cell.card?.timeSubValue1)!
        }

        if (program?.Metadata?.EndTime != nil) {
            let endDate = Formatter.dateFromISO8601(from: (program?.Metadata?.EndTime)!)
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

        cell.card?.liveBroadcastDelegate = self

        return cell
    }
}

extension Formatter {
    static let isoFormatter = ISO8601DateFormatter()

    static func dateFromISO8601(from dateString: String) -> Date? {
        var mdateString = dateString
        
        if #available(iOS 11.0, *) {
            isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        } else { // support for iOS10
            mdateString = dateString.replacingOccurrences(of: "\\.\\d+", with: "", options: .regularExpression)
        }
        return isoFormatter.date(from: mdateString)
    }
}
