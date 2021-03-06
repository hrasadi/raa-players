//
//  FeedView.swift
//  raa-ios-player
//
//  Created by Hamid on 1/24/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import PromiseKit
import UIKit
import Presentr
import os

class FeedContainerViewController : PlayerViewController {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}

class FeedViewController : UIViewController {

    private static let PERSONAL_FEED_SECTION = 0
    private static let PUBLIC_FEED_SECTION = 1

    private var personalFeedDelegate = PersonalFeedDelegate()
    private var publicFeedDelegate: PublicFeedDelegate?
    
    private var feedData: FeedData?

    @IBOutlet weak var spinner: UIActivityIndicatorView!
    @IBOutlet weak var tableView: UITableView!
    private var programDetailsViewController: ProgramDetailsViewController?
    
    // Redraw the whole view
    private static let FULL_REDRAW_PERIOD: TimeInterval = 30
    private var lastFullRedrawnTimestamp: TimeInterval?

    // Footer views
    private var personalFeedFooterView: UIView? = nil
    private var publicFeedFooterView: UIView? = nil
    
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
        public static let FOOTER_HEIGHT: CGFloat = 40
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
        
        self.publicFeedDelegate = PublicFeedDelegate(self)
    }

    override func viewDidLoad() {
        super.viewDidLoad();

        self.tableView.dataSource = self
        self.tableView.delegate = self        
        
        NotificationCenter.default.addObserver(self, selector: #selector(reloadLineups), name:  NSNotification.Name.UIApplicationDidBecomeActive, object: nil)

        Context.Instance.feedManager.registerEventListener(listenerObject: self)
        Context.Instance.playbackManager.registerEventListener(listenerObject: self)

        self.reloadLineups()
        self.fullRedraw()
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
    
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        coordinator.animate(alongsideTransition: nil, completion: { _ in
            self.fullRedraw()
        })
    }

    override func viewDidAppear(_ animated: Bool) {
        let currentDate = Date().timeIntervalSince1970
        
        if self.lastFullRedrawnTimestamp != nil {
            if currentDate - self.lastFullRedrawnTimestamp! > FeedViewController.FULL_REDRAW_PERIOD {
                self.reloadLineups()
                self.fullRedraw()
            }
        }
    }

    @objc private func reloadLineups() {
        Context.Instance.reloadLineups()
    }
    
    func fullRedraw() {
        self.lastFullRedrawnTimestamp = Date().timeIntervalSince1970

        self.programDetailsViewController = storyboard?.instantiateViewController(withIdentifier: "ProgramContent") as? ProgramDetailsViewController
        
        self.tableView?.isHidden = true
        
        self.spinner.startAnimating()
        self.view.bringSubview(toFront: self.spinner)
        self.spinner.center = UIScreen.main.bounds.center
        self.spinner.setNeedsLayout()

        
        firstly { () -> Promise<FeedData> in
            return Context.Instance.feedManager.pullData()
        }.done { feedData in
            self.feedData = feedData
            
            self.spinner.stopAnimating()
            self.spinner.hidesWhenStopped = true
            
            self.tableView?.isHidden = false
            self.tableView?.reloadData()
        }.catch { _ in
        }
    }
    
    class PersonalFeedDelegate : FeedEntryCardDelegate {
        func onPlayButtonClicked(_ requestedFeedEntryId: String) {
            Context.Instance.playbackManager.playPersonalFeed(requestedFeedEntryId)
        }
    }
    
    class PublicFeedDelegate : FeedEntryCardDelegate {
        private var parentViewController: FeedViewController
        
        init(_ parentViewController: FeedViewController) {
            self.parentViewController = parentViewController
        }
        
        func onPlayButtonClicked(_ requestedFeedEntryId: String) {
            // Do not trust the cards state, redetermine playable state with PlayabackManager
            if self.parentViewController.determinePlayableState(requestedFeedEntryId) == .CurrentlyPlaying {
                // Pause
                Context.Instance.playbackManager.togglePlaybackState()
            } else {
                if let publicFeedEntry = Context.Instance.feedManager.lookupPublicFeedEntry(requestedFeedEntryId) {
                    if Context.Instance.playbackManager.getLastPlaybackState((publicFeedEntry.getMediaPath())!) > 0.0 {
                        let controller = self.parentViewController.storyboard?.instantiateViewController(withIdentifier: "playbackModeRequesterModal") as! PlaybackModeRequesterModal
                        controller.requestedEntry = publicFeedEntry
                        self.parentViewController.customPresentViewController(self.parentViewController.presenter, viewController: controller, animated: true, completion: nil)
                    } else {
                        Context.Instance.playbackManager.playPublicFeed(requestedFeedEntryId)
                    }
                } else {
                    Context.Instance.playbackManager.playPublicFeed(requestedFeedEntryId)
                }
            }
        }
    }
}

extension FeedViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        if self.viewIfLoaded?.window != nil {
            if data == nil {
                self.fullRedraw()
            }
            if let _ = data as? FeedData {
                self.fullRedraw()
            }
            if let _ = data as? PlaybackState {
                self.refreshCardsPlayableState()
            }
        }
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
            return "برنامه‌های جاری رادیو"
        } else if section == FeedViewController.PERSONAL_FEED_SECTION {
            return "زمان‌بندی شده بر اساس مکان شما"
        }
        return nil // this should not happen
    }

    func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        view.semanticContentAttribute = .forceRightToLeft
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let textLabel: UILabel = UILabel()
        textLabel.font = UIFont.systemFont(ofSize: 12)
        textLabel.textColor = UIColor.gray
        textLabel.textAlignment = .center
        textLabel.adjustsFontSizeToFitWidth = true
        textLabel.minimumScaleFactor = 0.6
        textLabel.numberOfLines = 3

        if section == FeedViewController.PUBLIC_FEED_SECTION {
            if self.feedData?.publicFeed == nil || self.feedData?.publicFeed?.count == 0 {
                textLabel.text = "فعلا برنامه‌ای اینجا نیست. برنامه‌های جدید رادیو از دوشنبه شب تا جمعه شب هر هفته (به وقت شرق آمریکا) منتشر می‌شن."
                publicFeedFooterView = textLabel
                return publicFeedFooterView
            } else {
                publicFeedFooterView = nil
            }
        } else if section == FeedViewController.PERSONAL_FEED_SECTION {
            if self.feedData?.personalFeed == nil || self.feedData?.personalFeed?.count == 0 {
                textLabel.text = !Context.Instance.isFirstExecution ? "فعلا برنامه‌ای اینجا نیست" : "رسیدن بخیر! کمی زمان لازمه تا رادیو زمان‌بندی شما رو یاد بگیره!"
                personalFeedFooterView = textLabel
                return personalFeedFooterView
            } else {
                personalFeedFooterView = nil
            }
        }
        return UIView(frame: CGRect.zero)
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        if section == FeedViewController.PUBLIC_FEED_SECTION {
            if self.feedData?.publicFeed == nil || self.feedData?.publicFeed?.count == 0 {
                return Defaults.FOOTER_HEIGHT
            }
        }
        if section == FeedViewController.PERSONAL_FEED_SECTION {
            if self.feedData?.personalFeed == nil || self.feedData?.personalFeed?.count == 0 {
                return Defaults.FOOTER_HEIGHT
            }
        }
        return 0
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == FeedViewController.PUBLIC_FEED_SECTION {
            return self.feedData?.publicFeed?.count ?? 0
        } else if section == FeedViewController.PERSONAL_FEED_SECTION {
            return self.feedData?.personalFeed?.count ?? 0
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
        
        let feedEntry = self.feedData?.personalFeed?[indexPath.row]
        let entryProgramInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(feedEntry?.ProgramObject?.ProgramId)!]
        
        cell.card?.programId = feedEntry?.ProgramObject?.ProgramId
        cell.card?.shouldPresent(self.programDetailsViewController, from: self)
        
        cell.card?.feedEntryId = feedEntry?.Id
        
        if (feedEntry?.ProgramObject?.Title != nil) {
            cell.card?.programTitle = (feedEntry?.ProgramObject?.Title)!
        }
        if (feedEntry?.ProgramObject?.Subtitle != nil) {
            cell.card?.programSubtitle = (feedEntry?.ProgramObject?.Subtitle)!
        }

        if (feedEntry?.ReleaseTimestamp != nil) {
            let releaseDate = Date(timeIntervalSince1970: (feedEntry?.ReleaseTimestamp)!)
            cell.card?.timeTitle1 = "شروع"
            cell.card?.timeValue1 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: releaseDate)) ?? (cell.card?.timeValue1)!
            cell.card?.timeSubValue1 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(releaseDate)) ?? (cell.card?.timeSubValue1)!

            // If Program is in the future,  playback button should not be active
            if ((feedEntry?.ReleaseTimestamp)! > Date().timeIntervalSince1970) {
                cell.card?.actionable = false
            } else {
                cell.card?.actionable = true
            }
        }
        
        if (feedEntry?.ExpirationTimestamp != nil) {
            let expirationDate = Date(timeIntervalSince1970: (feedEntry?.ExpirationTimestamp)!)
            cell.card?.timeTitle2 = "پایان"
            cell.card?.timeValue2 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: expirationDate)!) ?? (cell.card?.timeValue2)!
            cell.card?.timeSubValue2 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(expirationDate)) ?? (cell.card?.timeSubValue2)!
        }
        
        cell.card?.backgroundImage = UIImage(data: entryProgramInfo?.bannerImageData ?? ProgramInfo.defaultBannerImageData)
        
        cell.card?.setNeedsDisplay()
        
        cell.card?.feedDelegate = self.personalFeedDelegate
        
        return cell
    }
    
    func generatePublicCell(_ tableView: UITableView, indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "feedCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? FeedEntryCardTableViewCell else {
            fatalError("The dequeued cell is not an instance of FeedEntryCardTableViewCell.")
        }
        
        let feedEntry = self.feedData?.publicFeed?[indexPath.row]
        let entryProgramInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(feedEntry?.ProgramObject?.ProgramId)!]
        
        cell.card?.programId = feedEntry?.ProgramObject?.ProgramId
        cell.card?.shouldPresent(self.programDetailsViewController, from: self)
        
        cell.card?.feedEntryId = feedEntry?.Id
        
        if (feedEntry?.ProgramObject?.Title != nil) {
            cell.card?.programTitle = (feedEntry?.ProgramObject?.Title)!
        }
        if (feedEntry?.ProgramObject?.Subtitle != nil) {
            cell.card?.programSubtitle = (feedEntry?.ProgramObject?.Subtitle)!
        }

        if (feedEntry?.ReleaseTimestamp != nil) {
            let releaseDate = Date(timeIntervalSince1970: (feedEntry?.ReleaseTimestamp)!)
            cell.card?.timeTitle1 = "انتشار"
            cell.card?.timeValue1 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: releaseDate)) ?? (cell.card?.timeValue1)!
            cell.card?.timeSubValue1 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(releaseDate)) ?? (cell.card?.timeSubValue1)!
        }
        
        if (feedEntry?.ExpirationTimestamp != nil) {
            let expirationDate = Date(timeIntervalSince1970: (feedEntry?.ExpirationTimestamp)!)
            cell.card?.timeTitle2 = "انقضا"
            cell.card?.timeValue2 = Utils.convertToPersianLocaleString(Utils.getHourOfDayString(from: expirationDate)!) ?? (cell.card?.timeValue2)!
            cell.card?.timeSubValue2 = Utils.convertToPersianLocaleString(Utils.getRelativeDayName(expirationDate)) ?? (cell.card?.timeSubValue2)!
        }
        
        cell.card?.backgroundImage = UIImage(data: entryProgramInfo?.bannerImageData ?? ProgramInfo.defaultBannerImageData)
        cell.card?.playableState = self.determinePlayableState(feedEntry?.Id)

        cell.card?.actionable = true
        cell.card?.feedDelegate = self.publicFeedDelegate
        
        cell.card?.setNeedsDisplay()
        
        return cell
    }

    private func refreshCardsPlayableState() {
        for entryCell in self.tableView.visibleCells {
            if let cell = entryCell as? FeedEntryCardTableViewCell {
                cell.card?.playableState = self.determinePlayableState(cell.card?.feedEntryId)
                cell.card?.setNeedsDisplay()
            }
        }
    }
    
    private func determinePlayableState(_ publicFeedEntryId: String?) -> PlayableState {
        if let publicFeedEntry = Context.Instance.feedManager.lookupPublicFeedEntry(publicFeedEntryId ?? "") {
            if ((Context.Instance.playbackManager.playbackState?.playing ?? false) && Context.Instance.playbackManager.playbackState?.mediaPath == publicFeedEntry.getMediaPath()) {
                return .CurrentlyPlaying
            } else {
                return .Playable
            }
        }
        return .Playable
    }
}
