//
//  ProgramArchiveViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/23/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import PromiseKit
import Presentr

class ProgramArchiveContainerViewController : PlayerViewController {
    public var programId: String?
    public var programTitle: String?
    
    @IBOutlet weak var containerView: UIView!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = programTitle
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == "programArchiveTableDisplay") {
            let programArchiveController = segue.destination as! ProgramArchiveViewController
            programArchiveController.programId = self.programId
        } else if (segue.identifier == "archivePodcastSegue") {
            let programArchivePodcastDescriptionController = segue.destination as! ProgramArchivePodcastDescriptionViewController
            programArchivePodcastDescriptionController.programId = self.programId
        }
    }
    
    @IBAction func podcastDescriptionClicked(_ sender: Any) {
        performSegue(withIdentifier: "archivePodcastSegue", sender: self)
    }
}

class ProgramArchiveViewController : UITableViewController {
    public var programId: String?
    
    private var dateFormatter: DateFormatter = DateFormatter()
    
    private var programArchive: [ArchiveEntry]?
    
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }

    let presenter: Presentr = {
        let customPresenter: Presentr = Presentr(presentationType: .bottomHalf)
        customPresenter.transitionType = .coverVertical
        customPresenter.dismissTransitionType = .crossDissolve
        customPresenter.roundCorners = true
        customPresenter.cornerRadius = 20
        customPresenter.blurBackground = true
        customPresenter.blurStyle = UIBlurEffectStyle.light
        customPresenter.dismissOnSwipe = true
        customPresenter.dismissOnSwipeDirection = .bottom
        return customPresenter
    }()
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)

        dateFormatter.dateFormat = "yyyy-MM-dd"
        self.dateFormatter.locale = Locale(identifier: "en_US")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        programArchive = Context.Instance.archiveManager.loadProgramArchiveSync(programId!)
        self.tableView.reloadData()
        
        Context.Instance.playbackManager.registerEventListener(listenerObject: self)
    }
    
    // FIXES A BUG THAT LAYOUTS TABLE WRONG IN IOS 10
    func fixTableViewInsets() {
        let zContentInsets = UIEdgeInsets.zero
        tableView.contentInset = zContentInsets
        tableView.scrollIndicatorInsets = zContentInsets
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        fixTableViewInsets()
    }
    // END BUG FIX
}

extension ProgramArchiveViewController : ProgramArchiveEntryDelegate {
    func onPlayButtonClicked(_ requestedArchiveEntry: ArchiveEntry?) {
        // Do not trust the cards state, redetermine playable state with PlayabackManager
        if self.determinePlayableState(requestedArchiveEntry) == .CurrentlyPlaying {
            // Pause
            Context.Instance.playbackManager.togglePlaybackState()
        } else {
            if Context.Instance.playbackManager.getLastPlaybackState((requestedArchiveEntry?.getMediaPath())!) > 0.0 {
                let controller = self.storyboard?.instantiateViewController(withIdentifier: "playbackModeRequesterModal") as! PlaybackModeRequesterModal
                controller.requestedEntry = requestedArchiveEntry
                customPresentViewController(presenter, viewController: controller, animated: true, completion: nil)
            } else {
                Context.Instance.playbackManager.playArchiveEntry(requestedArchiveEntry)
            }
        }
    }
}

extension ProgramArchiveViewController {
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Defaults.CELL_HEIGHT
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return programArchive?.count ?? 0
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "archiveEntryCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? ProgramArchiveEntryTableViewCell else {
            fatalError("The dequeued cell is not an instance of ProgramArchiveEntryTableViewCell.")
        }
        
        let row = indexPath.row
        
        cell.card?.archiveEntry = self.programArchive![row]
        
        cell.card?.programTitle = self.programArchive![row].Program?.Subtitle ?? "عنوان این برنامه‌ را پیدا نکردیم!"
        cell.card?.programSubtitle = ""
        
        let date = self.dateFormatter.date(from: self.programArchive![row].ReleaseDateString!)
        if (date != nil) {
            cell.card?.timeValue1 = Utils.getPersianLocaleDateString(date!)
        } else {
            cell.card?.timeValue1 = ""
        }
        
        cell.card?.backgroundImage = UIImage(data: ProgramInfo.defaultBannerImageData)
        cell.card?.playableState = self.determinePlayableState(self.programArchive![row])
        
        cell.card?.setNeedsDisplay()
        cell.card?.archiveEntryDelegate = self
        
        return cell
    }
    
    private func refreshCardsPlayableState() {
        for entryCell in self.tableView.visibleCells {
            let cell = entryCell as! ProgramArchiveEntryTableViewCell
            cell.card?.playableState = self.determinePlayableState(cell.card?.archiveEntry)
            cell.card?.setNeedsDisplay()
        }
    }
    
    private func determinePlayableState(_ archiveEntry: ArchiveEntry?) -> PlayableState {
        if ((Context.Instance.playbackManager.playbackState?.playing ?? false) && Context.Instance.playbackManager.playbackState?.mediaPath == archiveEntry?.getMediaPath()) {
            return .CurrentlyPlaying
        } else {
            return .Playable
        }
    }
}

extension ProgramArchiveViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        if self.viewIfLoaded?.window != nil {
            self.refreshCardsPlayableState()
        }
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

class ProgramArchivePodcastDescriptionViewController : UIViewController {
    var programId: String?
    
    @IBOutlet weak var PodcastURLTextView: UITextView!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        if (programId != nil) {
            self.PodcastURLTextView.text = Context.RSS_URL_PREFIX + "/" + programId! + ".xml"
        }
    }
    
    @IBAction func okButtonClicked(_ sender: Any) {
        self.dismiss(animated: true)
    }
}

