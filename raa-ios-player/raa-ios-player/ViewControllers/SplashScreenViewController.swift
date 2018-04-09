//
//  LaunchScreenViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/11/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import PromiseKit
import UIKit

class SplashScreenViewController : UIViewController {
    @IBOutlet weak var loadingStatusLbl: UILabel!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        // Startup services and managers, download data from server
        Context.initiateManagers(forceContextRenew: true)

        let q = DispatchQueue.global(qos: .background)

        self.loadingStatusLbl.text = "اول ببینیم رادیو چی داره..."

        firstly { () -> Promise<ProgramInfoDirectory> in
//            Context.Instance.liveBroadcastManager.pullData()
//        }.then { _ -> Promise<FeedData> in
//            self.loadingStatusLbl.text = "و البته برنامه‌های مخصوص خود خود شما..."
//            return Context.Instance.feedManager.pullData()
//        }.then { _ -> Promise<ProgramInfoDirectory> in
            self.loadingStatusLbl.text = "در آرشیو رادیو چی میگذره؟"
            return Context.Instance.programInfoDirectoryManager.pullData()
        }.then(on: q) { _ -> Promise<Bool> in
            DispatchQueue.main.async {
                self.loadingStatusLbl.text = "یه کم بزک دوزک!"
            }
            return Context.Instance.programInfoDirectoryManager.preloadImages()
        }.done { _ -> Void in
            self.loadingStatusLbl.text = "ایول! ردیف شد."
            OperationQueue.main.addOperation({
                self.performSegue(withIdentifier: "loadingComplete", sender: self)
            })
        }.catch { error in
        }
    }
}
