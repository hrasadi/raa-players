//
//  LaunchScreenViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/11/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class SplashScreenViewController : UIViewController {
    @IBOutlet weak var loadingStatusLbl: UILabel!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        // Startup services and managers, download data from server
        Context.initiateManagers()
        
        tryDownloadingPublicFeed()
    }
    
    func tryDownloadingPublicFeed() {
        loadingStatusLbl.text = "اول ببینیم رادیو چی داره..."

        class InterimFeedListener : ModelCommunicator {
            private var parent: SplashScreenViewController!
            
            init(parent: SplashScreenViewController) {
                self.parent = parent
            }
            
            func modelUpdated(data: Any?) {
                Context.Instance.feedManager.deregisterEventListener(listenerObject: self)
                self.parent.tryDownloadingProgramInfoDirectory()
            }
            
            func hashCode() -> Int {
                return ObjectIdentifier(self).hashValue
            }
        }
        
        let listener = InterimFeedListener(parent: self)
        Context.Instance.feedManager.registerEventListener(listenerObject: listener)
        
        // if data is already loaded
        let data = Context.Instance.feedManager.pullData()
        if (data != nil) {
            Context.Instance.feedManager.deregisterEventListener(listenerObject: listener)
            self.tryDownloadingProgramInfoDirectory()
        }
    }
    
    func tryDownloadingProgramInfoDirectory() {
        loadingStatusLbl.text = "فهرست برنامه‌ها چیز جدیدی نداره؟"

        class InterimPInfoDirectoryListener : ModelCommunicator {
            private var parent: SplashScreenViewController!
            
            init(parent: SplashScreenViewController) {
                self.parent = parent
            }
            
            func modelUpdated(data: Any?) {
                Context.Instance.feedManager.deregisterEventListener(listenerObject: self)
                self.parent.tryDownloadingImages()
            }
            
            func hashCode() -> Int {
                return ObjectIdentifier(self).hashValue
            }
        }
        
        let listener = InterimPInfoDirectoryListener(parent: self)
        Context.Instance.programInfoDirectoryManager.registerEventListener(listenerObject: listener)

        // if data is already loaded
        let data = Context.Instance.programInfoDirectoryManager.pullData()
        if (data != nil) {
            Context.Instance.feedManager.deregisterEventListener(listenerObject: listener)
            self.tryDownloadingImages()
        }
    }
    
    func tryDownloadingImages() {
        // Load supplementary data (images, etc)
        DispatchQueue.main.async {
            self.loadingStatusLbl.text = "یه کم بزک دوزک..."
        }
        
        Context.Instance.programInfoDirectoryManager.preloadImages() {() in
            DispatchQueue.main.async {
                self.loadingStatusLbl.text = "ایول! ردیف شد."
            }

            OperationQueue.main.addOperation({
                self.performSegue(withIdentifier: "loadingComplete", sender: self)
            })
        }
    }
}
